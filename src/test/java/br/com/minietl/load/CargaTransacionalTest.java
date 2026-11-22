package br.com.minietl.load;

import br.com.minietl.domain.enums.StatusProcessamento;
import br.com.minietl.exception.CargaException;
import br.com.minietl.service.ProcessamentoService;
import br.com.minietl.support.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import br.com.minietl.domain.repository.ClienteRepository;

import static br.com.minietl.support.CsvTestUtils.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.doThrow;

/** Uma falha durante a carga reverte toda a transação. */
class CargaTransacionalTest extends IntegracaoTest {

    @SpyBean
    private ClienteRepository clienteRepositorySpy;
    @Autowired
    private ProcessamentoService processamentoService;
    @Autowired
    private CargaService cargaService;

    @Test
    void falhaNaGravacaoRevertendoTudo() {
        Long id = processamentoService.processar("misto.csv", recurso("misto.csv")).getId();
        doThrow(new DataIntegrityViolationException("falha simulada")).when(clienteRepositorySpy).saveAll(anyIterable());

        assertThatThrownBy(() -> cargaService.carregar(id))
                .isInstanceOf(CargaException.class).hasMessageContaining("Nenhum registro foi carregado");

        assertThat(clienteRepository.count()).isZero();
        assertThat(processamentoRepository.findById(id).orElseThrow().getStatus())
                .isEqualTo(StatusProcessamento.PROCESSADO);
    }
}
