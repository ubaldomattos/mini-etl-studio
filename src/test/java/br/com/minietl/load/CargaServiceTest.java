package br.com.minietl.load;

import br.com.minietl.domain.entity.Cliente;
import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.domain.enums.StatusProcessamento;
import br.com.minietl.exception.OperacaoInvalidaException;
import br.com.minietl.exception.RecursoNaoEncontradoException;
import br.com.minietl.service.ProcessamentoService;
import br.com.minietl.support.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static br.com.minietl.support.CsvTestUtils.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes da operação de carga com SQLite. */
class CargaServiceTest extends IntegracaoTest {

    @Autowired
    private ProcessamentoService processamentoService;
    @Autowired
    private CargaService cargaService;

    @Test
    void carregaSomenteRegistrosValidos() {
        Long id = processamentoService.processar("misto.csv", recurso("misto.csv")).getId();

        Processamento p = cargaService.carregar(id);

        assertThat(p.getStatus()).isEqualTo(StatusProcessamento.CARREGADO);
        assertThat(p.getQtdCarregados()).isEqualTo(3);
        assertThat(clienteRepository.findAll()).extracting(Cliente::getEmail)
                .containsExactlyInAnyOrder("maria@exemplo.com", "joao@exemplo.com", "bia@exemplo.com");
    }

    @Test
    void gravaDadosTransformadosEVinculaRegistroAoCliente() {
        Long id = processamentoService.processar("misto.csv", recurso("misto.csv")).getId();
        cargaService.carregar(id);

        Cliente maria = clienteRepository.findByEmailIn(List.of("maria@exemplo.com")).get(0);
        assertThat(maria.getNome()).isEqualTo("Maria da Silva");
        assertThat(maria.getTelefone()).isEqualTo("31987654321");
        assertThat(maria.getCidade()).isEqualTo("Belo Horizonte");

        List<RegistroProcessamento> validos = registroRepository
                .findByProcessamentoIdAndSituacaoOrderByNumeroLinha(id, SituacaoRegistro.VALIDO);
        assertThat(validos).allSatisfy(r -> assertThat(r.getClienteId()).isNotNull());
        assertThat(validos.get(0).getClienteId()).isEqualTo(maria.getId());
    }

    @Test
    void naoPermiteCarregarDuasVezes() {
        Long id = processamentoService.processar("misto.csv", recurso("misto.csv")).getId();
        cargaService.carregar(id);

        assertThatThrownBy(() -> cargaService.carregar(id))
                .isInstanceOf(OperacaoInvalidaException.class).hasMessageContaining("já foi carregado");
        assertThat(clienteRepository.count()).isEqualTo(3);
    }

    @Test
    void reimportacaoDoMesmoArquivoNaoDuplicaClientes() {
        cargaService.carregar(processamentoService.processar("misto.csv", recurso("misto.csv")).getId());

        Processamento segundo = processamentoService.processar("misto.csv", recurso("misto.csv"));
        assertThat(segundo.getQtdValidos()).isZero();
        assertThat(segundo.getQtdDuplicados()).isEqualTo(4);

        Processamento carregado = cargaService.carregar(segundo.getId());
        assertThat(carregado.getQtdCarregados()).isZero();
        assertThat(clienteRepository.count()).isEqualTo(3);
    }

    @Test
    void reclassificaComoDuplicadoQuandoEmailFoiCarregadoPorOutroProcessamento() {
        Long primeiro = processamentoService.processar("validos.csv", recurso("validos.csv")).getId();
        Long segundo = processamentoService.processar("duplicados.csv", recurso("duplicados.csv")).getId();
        cargaService.carregar(primeiro);

        Processamento p = cargaService.carregar(segundo);

        assertThat(p.getQtdCarregados()).isZero();
        assertThat(p.getQtdValidos()).isZero();
        assertThat(p.getQtdDuplicados()).isEqualTo(4);
        RegistroProcessamento maria = registroRepository.findByProcessamentoIdAndSituacaoOrderByNumeroLinha(
                segundo, SituacaoRegistro.DUPLICADO).get(0);
        assertThat(maria.getMensagem()).contains("já cadastrado na base");
        assertThat(maria.getResultadoValidacao()).startsWith("Duplicidade identificada.")
                .contains("DUPLICIDADE=REPROVADA");
        assertThat(clienteRepository.count()).isEqualTo(3);
    }

    @Test
    void falhaParaProcessamentoInexistente() {
        assertThatThrownBy(() -> cargaService.carregar(999L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
