package br.com.minietl.transform;

import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.rule.CamposObrigatoriosRule;
import br.com.minietl.transform.rule.EmailFormatoRule;
import br.com.minietl.transform.rule.EstruturaRule;
import br.com.minietl.transform.rule.TelefoneFormatoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static br.com.minietl.transform.TransformTestFactory.registro;
import static org.assertj.core.api.Assertions.assertThat;

/** Testes das regras de transformação e de validação dos registros. */
class TransformPipelineTest {

    private final TransformPipeline pipeline = TransformTestFactory.pipeline();

    @Test
    void registroCorretoEhValidoETransformado() {
        RegistroAvaliado r = pipeline.avaliar(registro(2, "  maria da silva ", "MARIA@EXEMPLO.COM",
                "(31) 98765-4321", "belo horizonte"));

        assertThat(r.situacao()).isEqualTo(SituacaoRegistro.VALIDO);
        assertThat(r.campos()).isEqualTo(new CamposPadronizados("Maria da Silva", "maria@exemplo.com",
                "31987654321", "Belo Horizonte"));
        assertThat(r.mensagem()).isNull();
        assertThat(r.resultadoValidacao()).startsWith("Todas as validações foram aprovadas.")
                .contains("EMAIL_FORMATO=APROVADA", "TELEFONE_FORMATO=APROVADA");
    }

    @Test
    void preservaDadosOriginaisSeparadosDosTransformados() {
        RegistroExtraido original = registro(2, " ana ", "ANA@X.COM", "(81) 99999-8888", "recife");

        RegistroAvaliado r = pipeline.avaliar(original);

        assertThat(r.original()).isSameAs(original);
        assertThat(r.original().nome()).isEqualTo(" ana ");
        assertThat(r.campos().nome()).isEqualTo("Ana");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@exemplo", "ana.exemplo.com", "ana@@exemplo.com", "ana silva@exemplo.com",
            "ana@.com", "@exemplo.com", "ana..silva@exemplo.com"})
    void identificaEmailsInvalidos(String email) {
        RegistroAvaliado r = pipeline.avaliar(registro(2, "Ana", email, "81999998888", "Recife"));

        assertThat(r.situacao()).isEqualTo(SituacaoRegistro.INVALIDO);
        assertThat(r.mensagem()).contains("E-mail inválido");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@exemplo.com", "ana.silva+tag@sub.exemplo.com.br", "a_b-c@ex-emplo.org"})
    void aceitaEmailsValidos(String email) {
        assertThat(pipeline.avaliar(registro(2, "Ana", email, "81999998888", "Recife")).situacao())
                .isEqualTo(SituacaoRegistro.VALIDO);
    }

    @Test
    void identificaCamposObrigatoriosAusentes() {
        RegistroAvaliado r = pipeline.avaliar(registro(2, "  ", "", "81999998888", null));

        assertThat(r.situacao()).isEqualTo(SituacaoRegistro.INVALIDO);
        assertThat(r.mensagem()).contains("Campo(s) obrigatório(s) ausente(s): nome, e-mail, cidade");
        assertThat(r.resultadoValidacao()).contains("EMAIL_FORMATO=NAO_APLICAVEL");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "3198765432100", "(31) 18765-4321", "0131987654", "31 9abc-defg"})
    void identificaTelefonesInvalidos(String telefone) {
        RegistroAvaliado r = pipeline.avaliar(registro(2, "Ana", "ana@x.com", telefone, "Recife"));

        assertThat(r.situacao()).isEqualTo(SituacaoRegistro.INVALIDO);
        assertThat(r.mensagem()).contains("Telefone inválido");
    }

    @Test
    void informaTodosOsMotivosQuandoHaVariosProblemas() {
        RegistroAvaliado r = pipeline.avaliar(registro(2, "", "invalido", "12", "Recife"));

        assertThat(r.mensagem()).contains("ausente(s): nome").contains("E-mail inválido")
                .contains("Telefone inválido");
        assertThat(r.mensagem().split("; ")).hasSize(3);
    }

    @Test
    void registroComProblemaDeEstruturaEhInvalido() {
        RegistroExtraido incompleto = new RegistroExtraido(5, "Ana", "ana@x.com", "81999998888", null,
                "Ana,ana@x.com,81999998888", "Estrutura inconsistente: esperado(s) 4 coluna(s), encontrado(s) 3");

        RegistroAvaliado r = pipeline.avaliar(incompleto);

        assertThat(r.situacao()).isEqualTo(SituacaoRegistro.INVALIDO);
        assertThat(r.mensagem()).contains("Estrutura inconsistente").contains("cidade");
        assertThat(r.resultadoValidacao()).contains(EstruturaRule.CODIGO + "=REPROVADA");
    }

    @Test
    void codigosDasRegrasAparecemNoResultado() {
        String resultado = pipeline.avaliar(registro(2, "Ana", "ana@x.com", "81999998888", "Recife"))
                .resultadoValidacao();

        assertThat(resultado).contains(EstruturaRule.CODIGO, CamposObrigatoriosRule.CODIGO,
                EmailFormatoRule.CODIGO, TelefoneFormatoRule.CODIGO);
    }
}
