package br.com.minietl.export;

import br.com.minietl.exception.RecursoNaoEncontradoException;
import br.com.minietl.load.CargaService;
import br.com.minietl.service.ProcessamentoService;
import br.com.minietl.support.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static br.com.minietl.support.CsvTestUtils.lerExportado;
import static br.com.minietl.support.CsvTestUtils.recurso;
import static br.com.minietl.support.CsvTestUtils.utf8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes da geração das três modalidades de CSV. */
class CsvExportServiceTest extends IntegracaoTest {

    @Autowired
    private ProcessamentoService processamentoService;
    @Autowired
    private CargaService cargaService;
    @Autowired
    private CsvExportService exportService;

    private Long processarMisto() {
        return processamentoService.processar("misto.csv", recurso("misto.csv")).getId();
    }

    private List<String[]> exportar(Long id, TipoExportacao tipo) {
        return lerExportado(exportService.exportar(id, tipo).conteudo());
    }

    // ---------- CSV tratado ----------

    @Test
    void tratadoContemSomenteValidosCorrigidosESemDuplicidade() {
        List<String[]> linhas = exportar(processarMisto(), TipoExportacao.TRATADO);

        assertThat(linhas.get(0)).containsExactly("nome", "email", "telefone", "cidade");
        assertThat(linhas.subList(1, linhas.size())).containsExactly(
                new String[]{"Maria da Silva", "maria@exemplo.com", "31987654321", "Belo Horizonte"},
                new String[]{"João Pereira", "joao@exemplo.com", "1134567890", "São Paulo"},
                new String[]{"Oliveira, Beatriz", "bia@exemplo.com", "51988887777", "Porto Alegre"});

        List<String> emails = linhas.stream().skip(1).map(l -> l[1]).toList();
        assertThat(emails).doesNotHaveDuplicates();
    }

    @Test
    void tratadoCorrespondeAosRegistrosCarregadosNoBanco() {
        Long id = processarMisto();
        List<String[]> linhas = exportar(id, TipoExportacao.TRATADO);
        cargaService.carregar(id);

        List<String> emailsCsv = linhas.stream().skip(1).map(l -> l[1]).toList();
        List<String> emailsBanco = clienteRepository.findAll().stream().map(c -> c.getEmail()).toList();
        assertThat(emailsCsv).containsExactlyInAnyOrderElementsOf(emailsBanco);
    }

    // ---------- CSV de rejeitados ----------

    @Test
    void rejeitadosPreservaDadosOriginaisEInformaMotivo() {
        List<String[]> linhas = exportar(processarMisto(), TipoExportacao.REJEITADOS);

        assertThat(linhas.get(0)).containsExactly("numero_linha", "nome", "email", "telefone", "cidade",
                "tipo_rejeicao", "motivo_rejeicao");
        assertThat(linhas).hasSize(1 + 4);
        assertThat(linhas.stream().skip(1).map(l -> l[0])).containsExactly("4", "5", "6", "8");

        String[] ana = linhas.get(1);
        assertThat(ana).containsExactly("4", "ana costa", "ana@exemplo", "(21) 99876-5432", "rio de janeiro",
                "VALIDACAO", "E-mail inválido: 'ana@exemplo'");
    }

    @Test
    void rejeitadosInformaTodosOsMotivosDoRegistro() {
        String[] semNome = exportar(processarMisto(), TipoExportacao.REJEITADOS).get(2);

        assertThat(semNome[1]).isEmpty();
        assertThat(semNome[3]).isEqualTo("123");
        assertThat(semNome[6]).contains("Campo(s) obrigatório(s) ausente(s): nome").contains("Telefone inválido");
    }

    @Test
    void rejeitadosDiferenciaDuplicidadeDeErroDeValidacao() {
        List<String[]> linhas = exportar(processarMisto(), TipoExportacao.REJEITADOS);

        String[] duplicado = linhas.get(3);
        assertThat(duplicado[0]).isEqualTo("6");
        assertThat(duplicado[1]).isEqualTo("Maria Silva");
        assertThat(duplicado[2]).isEqualTo("maria@exemplo.com");
        assertThat(duplicado[3]).isEqualTo("31987654321");
        assertThat(duplicado[5]).isEqualTo("DUPLICIDADE");
        assertThat(duplicado[6]).isEqualTo("E-mail duplicado: já informado na linha 2");

        String[] estrutura = linhas.get(4);
        assertThat(estrutura[5]).isEqualTo("VALIDACAO");
        assertThat(estrutura[4]).isEmpty();
        assertThat(estrutura[6]).contains("Estrutura inconsistente");
    }

    // ---------- CSV completo ----------

    @Test
    void completoContemTodosOsRegistrosComSituacaoEResultado() {
        List<String[]> linhas = exportar(processarMisto(), TipoExportacao.COMPLETO);

        assertThat(linhas.get(0)).containsExactly("numero_linha", "nome", "email", "telefone", "cidade",
                "situacao", "resultado_validacao");
        assertThat(linhas).hasSize(1 + 7);
        assertThat(linhas.stream().skip(1).map(l -> l[5])).containsExactly(
                "VALIDO", "VALIDO", "INVALIDO", "INVALIDO", "DUPLICADO", "VALIDO", "INVALIDO");
    }

    @Test
    void completoPreservaOriginaisEDescreveValidacoes() {
        List<String[]> linhas = exportar(processarMisto(), TipoExportacao.COMPLETO);

        String[] maria = linhas.get(1);
        assertThat(maria[1]).isEqualTo("  maria da silva  ");
        assertThat(maria[2]).isEqualTo("MARIA@EXEMPLO.COM");
        assertThat(maria[6]).startsWith("Todas as validações foram aprovadas.");

        assertThat(linhas.get(3)[6]).startsWith("Validação reprovada.").contains("EMAIL_FORMATO=REPROVADA");
        assertThat(linhas.get(5)[6]).startsWith("Duplicidade identificada.").contains("linha 2");
    }

    // ---------- Regras gerais ----------

    @Test
    void arquivosUsamUtf8ComBom() {
        byte[] conteudo = exportService.exportar(processarMisto(), TipoExportacao.COMPLETO).conteudo();

        assertThat(Arrays.copyOf(conteudo, 3)).containsExactly(0xEF, 0xBB, 0xBF);
    }

    @Test
    void trataAspasVirgulasQuebrasDeLinhaEAcentos() {
        String csv = "nome,email,telefone,cidade\n"
                + "\"Ana \"\"Aninha\"\" Souza\",ana@x.com,81999998888,\"São Paulo, SP\"\n"
                + "\"Bia\nLinha\",bia@x,81999998888,Recife\n";
        Long id = processamentoService.processar("especiais.csv", utf8(csv)).getId();

        List<String[]> tratado = exportar(id, TipoExportacao.TRATADO);
        assertThat(tratado.get(1)[0]).isEqualTo("Ana \"Aninha\" Souza");
        assertThat(tratado.get(1)[3]).isEqualTo("São Paulo, Sp");

        List<String[]> rejeitados = exportar(id, TipoExportacao.REJEITADOS);
        assertThat(rejeitados.get(1)[1]).isEqualTo("Bia\nLinha");
    }

    @Test
    void nomeDoArquivoIdentificaTipoEProcessamento() {
        Long id = processarMisto();

        assertThat(exportService.exportar(id, TipoExportacao.REJEITADOS).nomeArquivo())
                .isEqualTo("rejeitados_proc" + id + ".csv");
    }

    @Test
    void falhaParaProcessamentoInexistente() {
        assertThatThrownBy(() -> exportService.exportar(12345L, TipoExportacao.TRATADO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- Histórico ----------

    @Test
    void exportaProcessamentosAnterioresDoHistorico() {
        Long antigo = processarMisto();
        cargaService.carregar(antigo);
        Long recente = processamentoService.processar("validos.csv", recurso("validos.csv")).getId();

        assertThat(exportar(antigo, TipoExportacao.COMPLETO)).hasSize(1 + 7);
        assertThat(exportar(antigo, TipoExportacao.REJEITADOS)).hasSize(1 + 4);
        assertThat(exportar(antigo, TipoExportacao.TRATADO)).hasSize(1 + 3);

        // No processamento recente, maria e joão já estavam na base: só Ana é válida.
        List<String[]> tratadoRecente = exportar(recente, TipoExportacao.TRATADO);
        assertThat(tratadoRecente).hasSize(1 + 1);
        assertThat(tratadoRecente.get(1)[1]).isEqualTo("ana@exemplo.com");
        assertThat(exportar(recente, TipoExportacao.REJEITADOS).stream().skip(1).map(l -> l[6]))
                .allMatch(m -> m.contains("já cadastrado na base"));
    }
}
