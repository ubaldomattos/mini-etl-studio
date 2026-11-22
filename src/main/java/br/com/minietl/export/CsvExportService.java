package br.com.minietl.export;

import br.com.minietl.config.AppProperties;
import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.domain.repository.RegistroProcessamentoRepository;
import br.com.minietl.exception.RecursoNaoEncontradoException;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Gera dinamicamente os CSVs de um processamento. Os dados vêm exclusivamente dos registros do
 * processamento (e não da tabela de clientes), de modo que a exportação funciona para
 * registros inválidos/duplicados e também para processamentos antigos do histórico.
 */
@Service
public class CsvExportService {

    private static final Logger log = LoggerFactory.getLogger(CsvExportService.class);
    private static final byte[] BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final int TAMANHO_LOTE = 500;

    private final Map<TipoExportacao, ExportStrategy> estrategias = new EnumMap<>(TipoExportacao.class);
    private final ProcessamentoRepository processamentoRepository;
    private final RegistroProcessamentoRepository registroRepository;
    private final AppProperties properties;

    public CsvExportService(List<ExportStrategy> estrategias, ProcessamentoRepository processamentoRepository,
                            RegistroProcessamentoRepository registroRepository, AppProperties properties) {
        estrategias.forEach(e -> this.estrategias.put(e.tipo(), e));
        this.processamentoRepository = processamentoRepository;
        this.registroRepository = registroRepository;
        this.properties = properties;
    }

    public ArquivoCsv exportar(Long processamentoId, TipoExportacao tipo) {
        if (!processamentoRepository.existsById(processamentoId)) {
            throw new RecursoNaoEncontradoException("Processamento #" + processamentoId + " não encontrado.");
        }
        ExportStrategy estrategia = estrategias.get(tipo);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(saida, StandardCharsets.UTF_8)) {
            saida.write(BOM_UTF8);
            ICSVWriter csv = new CSVWriterBuilder(writer)
                    .withSeparator(properties.export().delimiter())
                    .withLineEnd("\r\n")
                    .build();
            csv.writeNext(estrategia.cabecalho(), false);
            escreverRegistros(processamentoId, estrategia, csv);
            csv.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar o CSV " + tipo.getSlug(), e);
        }
        log.info("CSV '{}' gerado para o processamento #{} ({} bytes)", tipo.getSlug(), processamentoId,
                saida.size());
        return new ArquivoCsv(tipo.getSlug() + "_proc" + processamentoId + ".csv", saida.toByteArray());
    }

    private void escreverRegistros(Long processamentoId, ExportStrategy estrategia, ICSVWriter csv) {
        PageRequest pagina = PageRequest.of(0, TAMANHO_LOTE, Sort.by("numeroLinha").and(Sort.by("id")));
        Slice<RegistroProcessamento> lote;
        do {
            lote = registroRepository.findByProcessamentoIdAndSituacaoIn(processamentoId,
                    estrategia.situacoes(), pagina);
            lote.forEach(r -> csv.writeNext(valoresSemNulo(estrategia.linha(r)), false));
            pagina = pagina.next();
        } while (lote.hasNext());
    }

    private String[] valoresSemNulo(String[] valores) {
        String[] saida = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            saida[i] = valores[i] == null ? "" : valores[i];
        }
        return saida;
    }
}
