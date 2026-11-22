package br.com.minietl.service;

import br.com.minietl.domain.repository.ClienteRepository;
import br.com.minietl.dto.ClienteDto;
import br.com.minietl.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta dos clientes armazenados no banco. */
@Service
@Transactional(readOnly = true)
public class ClienteService {

    private static final int TAMANHO_PAGINA = 15;

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Page<ClienteDto> buscar(String termo, int pagina) {
        String filtro = termo == null ? "" : termo.strip();
        PageRequest pageable = PageRequest.of(Math.max(0, pagina), TAMANHO_PAGINA, Sort.by("nome").and(Sort.by("id")));
        return repository.buscar(filtro, pageable).map(ClienteDto::de);
    }

    public ClienteDto obter(Long id) {
        return repository.findById(id).map(ClienteDto::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente #" + id + " não encontrado."));
    }
}
