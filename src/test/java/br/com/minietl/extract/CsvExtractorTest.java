package br.com.minietl.extract;

import br.com.minietl.exception.ArquivoInvalidoException;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static br.com.minietl.support.CsvTestUtils.recurso;
import static br.com.minietl.support.CsvTestUtils.utf8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvExtractorTest {

    private final CsvExtractor extractor = new CsvExtractor();

    @Test
    void extraiRegistrosPreservandoValoresOriginais() {
        ExtracaoResultado resultado = extractor.extrair(recurso("misto.csv"));

        assertThat(resultado.registros()).hasSize(7);
        assertThat(resultado.delimitador()).isEqualTo(',');
        assertThat(resultado.cabecalhos()).containsExactly("nome", "email", "telefone", "cidade");

        RegistroExtraido primeiro = resultado.registros().get(0);
        assertThat(primeiro.numeroLinha()).isEqualTo(2);
        assertThat(primeiro.nome()).isEqualTo("  maria da silva  ");
        assertThat(primeiro.email()).isEqualTo("MARIA@EXEMPLO.COM");
    }

    @Test
    void trataCamposEntreAspasComVirgula() {
        RegistroExtraido beatriz = extractor.extrair(recurso("misto.csv")).registros().get(5);

        assertThat(beatriz.nome()).isEqualTo("Oliveira, Beatriz");
        assertThat(beatriz.temProblemaEstrutura()).isFalse();
    }

    @Test
    void identificaRegistroComEstruturaIncompleta() {
        ExtracaoResultado resultado = extractor.extrair(recurso("misto.csv"));
        RegistroExtraido ultimo = resultado.registros().get(6);

        assertThat(ultimo.temProblemaEstrutura()).isTrue();
        assertThat(ultimo.problemaEstrutura()).contains("esperado(s) 4", "encontrado(s) 3");
        assertThat(ultimo.cidade()).isNull();
        assertThat(resultado.quantidadeComProblemaEstrutura()).isEqualTo(1);
    }

    @Test
    void detectaPontoEVirgulaECabecalhosComAcentoEMaiusculas() {
        ExtracaoResultado resultado = extractor.extrair(recurso("validos.csv"));

        assertThat(resultado.delimitador()).isEqualTo(';');
        assertThat(resultado.registros()).hasSize(3);
        assertThat(resultado.registros().get(1).email()).isEqualTo("joao@exemplo.com");
    }

    @Test
    void aceitaColunasEmOutraOrdemEIgnoraColunasExtras() {
        String csv = "Cidade,Telefone,Observação,E-mail,Nome\nRecife,81999998888,vip,a@b.com,Ana\n";

        ExtracaoResultado resultado = extractor.extrair(utf8(csv));

        RegistroExtraido r = resultado.registros().get(0);
        assertThat(r.nome()).isEqualTo("Ana");
        assertThat(r.email()).isEqualTo("a@b.com");
        assertThat(r.cidade()).isEqualTo("Recife");
        assertThat(resultado.avisos()).anyMatch(a -> a.contains("Observação"));
    }

    @Test
    void ignoraBomUtf8ELinhasEmBranco() {
        String csv = "\uFEFFnome,email,telefone,cidade\n\nAna,a@b.com,81999998888,Recife\n,,,\n";

        ExtracaoResultado resultado = extractor.extrair(utf8(csv));

        assertThat(resultado.cabecalhos().get(0)).isEqualTo("nome");
        assertThat(resultado.registros()).hasSize(1);
        assertThat(resultado.registros().get(0).numeroLinha()).isEqualTo(3);
        assertThat(resultado.linhasEmBrancoIgnoradas()).isEqualTo(2);
    }

    @Test
    void preservaCampoComQuebraDeLinhaENumeracaoDaLinhaFisica() {
        String csv = "nome,email,telefone,cidade\n\"Ana\nMaria\",a@b.com,81999998888,Recife\nBia,b@b.com,81999998888,Recife\n";

        ExtracaoResultado resultado = extractor.extrair(utf8(csv));

        assertThat(resultado.registros().get(0).nome()).isEqualTo("Ana\nMaria");
        assertThat(resultado.registros().get(1).numeroLinha()).isEqualTo(4);
    }

    @Test
    void decodificaArquivoWindows1252() {
        byte[] conteudo = "nome,email,telefone,cidade\nJoão,j@b.com,81999998888,São Paulo\n"
                .getBytes(Charset.forName("windows-1252"));

        ExtracaoResultado resultado = extractor.extrair(conteudo);

        assertThat(resultado.registros().get(0).nome()).isEqualTo("João");
        assertThat(resultado.avisos()).anyMatch(a -> a.contains("Windows-1252"));
    }

    @Test
    void rejeitaArquivoSemColunaObrigatoria() {
        assertThatThrownBy(() -> extractor.extrair(recurso("sem_coluna_cidade.csv")))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining("Colunas obrigatórias ausentes: Cidade");
    }

    @Test
    void rejeitaArquivoVazioOuSomenteCabecalho() {
        assertThatThrownBy(() -> extractor.extrair(new byte[0]))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("vazio");
        assertThatThrownBy(() -> extractor.extrair(utf8("nome,email,telefone,cidade\n")))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("nenhum registro");
    }

    @Test
    void rejeitaArquivoBinario() {
        assertThatThrownBy(() -> extractor.extrair(new byte[]{'P', 'K', 3, 4, 0, 0}))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("binários");
    }

    @Test
    void normalizaChaveDoCabecalho() {
        assertThat(CsvExtractor.chaveDe(" E-MAIL ")).isEqualTo("email");
        assertThat(CsvExtractor.chaveDe("Telefone")).isEqualTo("telefone");
    }
}
