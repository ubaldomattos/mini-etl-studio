package br.com.minietl.service;

import br.com.minietl.config.AppProperties;
import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.domain.repository.ClienteRepository;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.domain.repository.RegistroProcessamentoRepository;
import br.com.minietl.dto.LinhaPreviaDto;
import br.com.minietl.dto.PreviaDto;
import br.com.minietl.exception.ArquivoInvalidoException;
import br.com.minietl.extract.CsvExtractor;
import br.com.minietl.extract.ExtracaoResultado;
import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.CamposPadronizados;
import br.com.minietl.transform.DuplicateDetector;
import br.com.minietl.transform.RegistroAvaliado;
import br.com.minietl.transform.TransformPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Orquestra as etapas Extract e Transform e persiste o resultado como um processamento
 * (histórico + registros com dados originais e transformados). A carga é feita por
 * {@link br.com.minietl.load.CargaService}.
 */
@Service
public class ProcessamentoService {

    private static final Logger log = LoggerFactory.getLogger(ProcessamentoService.class);

    private final CsvExtractor extractor;
    private final TransformPipeline pipeline;
    private final DuplicateDetector duplicateDetector;
    private final ClienteRepository clienteRepository;
    private final ProcessamentoRepository processamentoRepository;
    private final RegistroProcessamentoRepository registroRepository;
    private final AppProperties properties;

    public ProcessamentoService(CsvExtractor extractor, TransformPipeline pipeline,
                                DuplicateDetector duplicateDetector, ClienteRepository clienteRepository,
                                ProcessamentoRepository processamentoRepository,
                                RegistroProcessamentoRepository registroRepository, AppProperties properties) {
        this.extractor = extractor;
        this.pipeline = pipeline;
        this.duplicateDetector = duplicateDetector;
        this.clienteRepository = clienteRepository;
        this.processamentoRepository = processamentoRepository;
        this.registroRepository = registroRepository;
        this.properties = properties;
    }

    /** Lê o arquivo e devolve a prévia. Nada é gravado. */
    public PreviaDto gerarPrevia(String nomeArquivo, byte[] conteudo) {
        validarNomeArquivo(nomeArquivo);
        ExtracaoResultado extracao = extractor.extrair(conteudo);
        int limite = Math.max(1, properties.preview().linhas());
        List<LinhaPreviaDto> linhas = extracao.registros().stream().limit(limite)
                .map(r -> new LinhaPreviaDto(r.numeroLinha(), r.nome(), r.email(), r.telefone(), r.cidade(),
                        r.problemaEstrutura()))
                .toList();
        return new PreviaDto(nomeArquivo, descreverDelimitador(extracao.delimitador()), extracao.cabecalhos(),
                extracao.avisos(), extracao.registros().size(), extracao.quantidadeComProblemaEstrutura(),
                limite, linhas);
    }

    /** Executa Extract + Transform e persiste o processamento com todos os registros. */
    @Transactional
    public Processamento processar(String nomeArquivo, byte[] conteudo) {
        validarNomeArquivo(nomeArquivo);
        log.info("Processando arquivo '{}' ({} bytes)", nomeArquivo, conteudo == null ? 0 : conteudo.length);

        List<RegistroExtraido> extraidos = extractor.extrair(conteudo).registros();
        List<RegistroAvaliado> avaliados = pipeline.avaliar(extraidos);
        Map<String, Long> emailsNaBase = clienteRepository.mapearIdsPorEmail(
                avaliados.stream().filter(a -> a.situacao() == SituacaoRegistro.VALIDO)
                        .map(a -> a.campos().email()).toList());
        List<RegistroAvaliado> classificados = duplicateDetector.aplicar(avaliados, emailsNaBase);

        Processamento processamento = processamentoRepository.save(new Processamento(
                nomeArquivo, classificados.size(),
                contar(classificados, SituacaoRegistro.VALIDO),
                contar(classificados, SituacaoRegistro.INVALIDO),
                contar(classificados, SituacaoRegistro.DUPLICADO)));

        registroRepository.saveAll(classificados.stream().map(a -> paraEntidade(processamento, a)).toList());
        log.info("Processamento #{} concluído: {} extraídos, {} válidos, {} inválidos, {} duplicados",
                processamento.getId(), processamento.getQtdExtraidos(), processamento.getQtdValidos(),
                processamento.getQtdInvalidos(), processamento.getQtdDuplicados());
        return processamento;
    }

    private void validarNomeArquivo(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new ArquivoInvalidoException("Formato inválido: selecione um arquivo com extensão .csv.");
        }
    }

    private int contar(List<RegistroAvaliado> registros, SituacaoRegistro situacao) {
        return (int) registros.stream().filter(r -> r.situacao() == situacao).count();
    }

    private RegistroProcessamento paraEntidade(Processamento processamento, RegistroAvaliado a) {
        RegistroExtraido o = a.original();
        CamposPadronizados c = a.campos();
        RegistroProcessamento registro = new RegistroProcessamento(processamento, o.numeroLinha());
        registro.definirOriginais(o.nome(), o.email(), o.telefone(), o.cidade(), o.linhaBruta());
        registro.definirTransformados(vazioParaNulo(c.nome()), vazioParaNulo(c.email()),
                vazioParaNulo(c.telefone()), vazioParaNulo(c.cidade()));
        registro.definirResultado(a.situacao(), a.resultadoValidacao(), a.mensagem(), a.duplicadoDaLinha());
        return registro;
    }

    private String vazioParaNulo(String valor) {
        return valor == null || valor.isEmpty() ? null : valor;
    }

    private String descreverDelimitador(char delimitador) {
        return switch (delimitador) {
            case ';' -> "ponto e vírgula (;)";
            case '\t' -> "tabulação";
            default -> "vírgula (,)";
        };
    }
}
