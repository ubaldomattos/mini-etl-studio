package br.com.minietl.load;

import br.com.minietl.domain.entity.Cliente;
import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.domain.enums.StatusProcessamento;
import br.com.minietl.domain.repository.ClienteRepository;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.domain.repository.RegistroProcessamentoRepository;
import br.com.minietl.exception.CargaException;
import br.com.minietl.exception.OperacaoInvalidaException;
import br.com.minietl.exception.RecursoNaoEncontradoException;
import br.com.minietl.transform.ResultadoValidacaoFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Etapa Load: grava no banco os registros válidos de um processamento, em uma única transação.
 *
 * <p>Somente registros {@code VALIDO} são carregados. Se um e-mail passou a existir na base entre
 * o processamento e a carga (por exemplo, outra importação carregada antes), o registro é
 * reclassificado como {@code DUPLICADO} em vez de ser inserido.
 */
@Service
public class CargaService {

    private static final Logger log = LoggerFactory.getLogger(CargaService.class);

    private final ProcessamentoRepository processamentoRepository;
    private final RegistroProcessamentoRepository registroRepository;
    private final ClienteRepository clienteRepository;

    public CargaService(ProcessamentoRepository processamentoRepository,
                        RegistroProcessamentoRepository registroRepository, ClienteRepository clienteRepository) {
        this.processamentoRepository = processamentoRepository;
        this.registroRepository = registroRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Processamento carregar(Long processamentoId) {
        Processamento processamento = processamentoRepository.findById(processamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Processamento #" + processamentoId + " não encontrado."));
        if (processamento.getStatus() == StatusProcessamento.CARREGADO) {
            throw new OperacaoInvalidaException("O processamento #" + processamentoId + " já foi carregado.");
        }

        try {
            return executarCarga(processamento);
        } catch (DataAccessException e) {
            log.error("Falha na carga do processamento #{}", processamentoId, e);
            throw new CargaException("Falha ao gravar os dados no banco. Nenhum registro foi carregado.", e);
        }
    }

    private Processamento executarCarga(Processamento processamento) {
        List<RegistroProcessamento> validos = registroRepository
                .findByProcessamentoIdAndSituacaoOrderByNumeroLinha(processamento.getId(), SituacaoRegistro.VALIDO);
        Map<String, Long> emailsNaBase = clienteRepository.mapearIdsPorEmail(
                validos.stream().map(RegistroProcessamento::getTransEmail).toList());

        List<Cliente> novos = new ArrayList<>();
        List<RegistroProcessamento> carregados = new ArrayList<>();
        for (RegistroProcessamento registro : validos) {
            Long clienteExistente = emailsNaBase.get(registro.getTransEmail());
            if (clienteExistente != null) {
                reclassificarComoDuplicado(processamento, registro, clienteExistente);
            } else {
                novos.add(new Cliente(registro.getTransNome(), registro.getTransEmail(),
                        registro.getTransTelefone(), registro.getTransCidade()));
                carregados.add(registro);
            }
        }

        clienteRepository.saveAll(novos);
        for (int i = 0; i < novos.size(); i++) {
            carregados.get(i).vincularCliente(novos.get(i).getId());
        }
        processamento.registrarCarga(novos.size());
        log.info("Carga do processamento #{} concluída: {} registro(s) gravado(s)", processamento.getId(),
                novos.size());
        return processamento;
    }

    private void reclassificarComoDuplicado(Processamento processamento, RegistroProcessamento registro,
                                            Long clienteId) {
        String motivo = "E-mail já cadastrado na base de dados (cliente #" + clienteId + ")";
        registro.definirResultado(SituacaoRegistro.DUPLICADO,
                ResultadoValidacaoFormatter.comoDuplicadoNaBase(registro.getResultadoValidacao(), motivo),
                motivo, null);
        processamento.reclassificarValidoComoDuplicado();
        log.info("Registro da linha {} reclassificado como duplicado na carga", registro.getNumeroLinha());
    }
}
