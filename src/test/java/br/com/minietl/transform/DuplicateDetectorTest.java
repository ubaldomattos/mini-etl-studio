package br.com.minietl.transform;

import br.com.minietl.domain.enums.SituacaoRegistro;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static br.com.minietl.transform.TransformTestFactory.registro;
import static org.assertj.core.api.Assertions.assertThat;

class DuplicateDetectorTest {

    private final TransformPipeline pipeline = TransformTestFactory.pipeline();
    private final DuplicateDetector detector = new DuplicateDetector();

    @Test
    void marcaComoDuplicadoOsRegistrosSeguintesComMesmoEmailNormalizado() {
        List<RegistroAvaliado> avaliados = pipeline.avaliar(List.of(
                registro(2, "Maria", "maria@x.com", "31987654321", "BH"),
                registro(3, "Maria S", " MARIA@X.COM ", "31987654321", "BH"),
                registro(4, "João", "joao@x.com", "1134567890", "SP")));

        List<RegistroAvaliado> resultado = detector.aplicar(avaliados, Map.of());

        assertThat(resultado).extracting(RegistroAvaliado::situacao).containsExactly(
                SituacaoRegistro.VALIDO, SituacaoRegistro.DUPLICADO, SituacaoRegistro.VALIDO);
        RegistroAvaliado duplicado = resultado.get(1);
        assertThat(duplicado.duplicadoDaLinha()).isEqualTo(2);
        assertThat(duplicado.mensagem()).isEqualTo("E-mail duplicado: já informado na linha 2");
        assertThat(duplicado.resultadoValidacao()).startsWith("Duplicidade identificada.")
                .contains("DUPLICIDADE=REPROVADA");
    }

    @Test
    void registroInvalidoNaoEhConsideradoOriginalDeDuplicidade() {
        List<RegistroAvaliado> avaliados = pipeline.avaliar(List.of(
                registro(2, "", "maria@x.com", "31987654321", "BH"),
                registro(3, "Maria", "maria@x.com", "31987654321", "BH")));

        List<RegistroAvaliado> resultado = detector.aplicar(avaliados, Map.of());

        assertThat(resultado).extracting(RegistroAvaliado::situacao)
                .containsExactly(SituacaoRegistro.INVALIDO, SituacaoRegistro.VALIDO);
        assertThat(resultado.get(0).resultadoValidacao()).contains("DUPLICIDADE=NAO_APLICAVEL");
    }

    @Test
    void emailJaExistenteNaBaseEhDuplicado() {
        List<RegistroAvaliado> avaliados = pipeline.avaliar(List.of(
                registro(2, "Maria", "maria@x.com", "31987654321", "BH")));

        RegistroAvaliado r = detector.aplicar(avaliados, Map.of("maria@x.com", 7L)).get(0);

        assertThat(r.situacao()).isEqualTo(SituacaoRegistro.DUPLICADO);
        assertThat(r.mensagem()).contains("já cadastrado na base", "cliente #7");
        assertThat(r.duplicadoDaLinha()).isNull();
    }

    @Test
    void validosUnicosRecebemDuplicidadeAprovada() {
        RegistroAvaliado r = detector.aplicar(pipeline.avaliar(List.of(
                registro(2, "Ana", "ana@x.com", "81999998888", "Recife"))), Map.of()).get(0);

        assertThat(r.situacao()).isEqualTo(SituacaoRegistro.VALIDO);
        assertThat(r.resultadoValidacao()).contains("DUPLICIDADE=APROVADA");
    }
}
