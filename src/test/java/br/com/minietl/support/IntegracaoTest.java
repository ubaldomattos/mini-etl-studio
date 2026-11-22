package br.com.minietl.support;

import br.com.minietl.domain.repository.ClienteRepository;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.domain.repository.RegistroProcessamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Base dos testes de integração: contexto completo com SQLite em arquivo temporário. */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegracaoTest {

    @Autowired
    protected ClienteRepository clienteRepository;
    @Autowired
    protected ProcessamentoRepository processamentoRepository;
    @Autowired
    protected RegistroProcessamentoRepository registroRepository;

    @BeforeEach
    void limparBase() {
        registroRepository.deleteAllInBatch();
        processamentoRepository.deleteAllInBatch();
        clienteRepository.deleteAllInBatch();
    }
}
