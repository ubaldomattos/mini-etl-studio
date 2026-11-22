package br.com.minietl.service;

import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.domain.repository.RegistroProcessamentoRepository;
import br.com.minietl.dto.ProcessamentoDto;
import br.com.minietl.dto.RegistroDto;
import br.com.minietl.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta do histórico de processamentos e dos seus registros. */
@Service
@Transactional(readOnly = true)
public class HistoricoService {

    private static final int TAMANHO_PAGINA_HISTORICO = 10;
    private static final int TAMANHO_PAGINA_REGISTROS = 20;

    private final ProcessamentoRepository processamentoRepository;
    private final RegistroProcessamentoRepository registroRepository;

    public HistoricoService(ProcessamentoRepository processamentoRepository,
                            RegistroProcessamentoRepository registroRepository) {
        this.processamentoRepository = processamentoRepository;
        this.registroRepository = registroRepository;
    }

    public Page<ProcessamentoDto> listar(int pagina) {
        Pageable pageable = PageRequest.of(Math.max(0, pagina), TAMANHO_PAGINA_HISTORICO);
        return processamentoRepository.findAllByOrderByDataExecucaoDescIdDesc(pageable).map(ProcessamentoDto::de);
    }

    public ProcessamentoDto obter(Long id) {
        return ProcessamentoDto.de(buscar(id));
    }

    /** Registros do processamento, opcionalmente filtrados por situação. */
    public Page<RegistroDto> listarRegistros(Long id, SituacaoRegistro situacao, int pagina) {
        buscar(id);
        Pageable pageable = PageRequest.of(Math.max(0, pagina), TAMANHO_PAGINA_REGISTROS,
                Sort.by("numeroLinha").and(Sort.by("id")));
        var registros = situacao == null
                ? registroRepository.findByProcessamentoId(id, pageable)
                : registroRepository.findByProcessamentoIdAndSituacao(id, situacao, pageable);
        return registros.map(RegistroDto::de);
    }

    private Processamento buscar(Long id) {
        return processamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processamento #" + id + " não encontrado."));
    }
}
