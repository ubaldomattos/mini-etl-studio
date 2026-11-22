package br.com.minietl.domain;

import br.com.minietl.config.AppProperties;
import br.com.minietl.domain.entity.Cliente;
import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.dto.TotaisDto;
import br.com.minietl.support.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes de persistência com SQLite real (arquivo criado pelo Flyway). */
class PersistenciaSqliteTest extends IntegracaoTest {

    @Autowired
    private AppProperties properties;

    @Test
    void criaArquivoDoBancoAutomaticamente() {
        assertThat(Files.exists(Path.of(properties.dbPath()))).isTrue();
    }

    @Test
    void persisteERecuperaCliente() {
        Cliente salvo = clienteRepository.save(new Cliente("Ana Costa", "ana@x.com", "81999998888", "Recife"));

        Cliente lido = clienteRepository.findById(salvo.getId()).orElseThrow();
        assertThat(lido.getNome()).isEqualTo("Ana Costa");
        assertThat(lido.getDataCriacao()).isNotNull();
    }

    @Test
    void emailDoClienteEhUnico() {
        clienteRepository.saveAndFlush(new Cliente("Ana", "ana@x.com", "81999998888", "Recife"));

        // O dialeto SQLite traduz a violação como JpaSystemException (subclasse de DataAccessException).
        assertThatThrownBy(() -> clienteRepository.saveAndFlush(
                new Cliente("Outra Ana", "ana@x.com", "81999998888", "Recife")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("UNIQUE constraint failed: cliente.email");
    }

    @Test
    void buscaClientesPorNomeOuEmailSemDiferenciarMaiusculas() {
        clienteRepository.save(new Cliente("Maria Silva", "maria@x.com", "31987654321", "BH"));
        clienteRepository.save(new Cliente("João Pereira", "joao@empresa.com", "1134567890", "SP"));

        assertThat(clienteRepository.buscar("MARIA", PageRequest.of(0, 10))).hasSize(1);
        assertThat(clienteRepository.buscar("empresa", PageRequest.of(0, 10))).hasSize(1);
        assertThat(clienteRepository.buscar("", PageRequest.of(0, 10))).hasSize(2);
    }

    @Test
    void persisteRegistroComDadosOriginaisETransformados() {
        Processamento p = processamentoRepository.save(new Processamento("a.csv", 1, 0, 1, 0));
        RegistroProcessamento r = new RegistroProcessamento(p, 2);
        r.definirOriginais(" ana ", "ANA@X", "12", "recife", " ana ,ANA@X,12,recife");
        r.definirTransformados("Ana", "ana@x", "12", "Recife");
        r.definirResultado(SituacaoRegistro.INVALIDO, "Validação reprovada. ...", "E-mail inválido", null);
        registroRepository.save(r);

        RegistroProcessamento lido = registroRepository.findById(r.getId()).orElseThrow();
        assertThat(lido.getOrigNome()).isEqualTo(" ana ");
        assertThat(lido.getTransNome()).isEqualTo("Ana");
        assertThat(lido.getSituacao()).isEqualTo(SituacaoRegistro.INVALIDO);
        assertThat(lido.getLinhaBruta()).isEqualTo(" ana ,ANA@X,12,recife");
    }

    @Test
    void somaTotaisDoHistorico() {
        processamentoRepository.save(new Processamento("a.csv", 10, 6, 3, 1));
        processamentoRepository.save(new Processamento("b.csv", 5, 5, 0, 0));

        TotaisDto totais = processamentoRepository.somarTotais();

        assertThat(totais).isEqualTo(new TotaisDto(15, 11, 3, 1, 0));
    }

    @Test
    void totaisSemProcessamentosSaoZero() {
        assertThat(processamentoRepository.somarTotais()).isEqualTo(new TotaisDto(0, 0, 0, 0, 0));
    }
}
