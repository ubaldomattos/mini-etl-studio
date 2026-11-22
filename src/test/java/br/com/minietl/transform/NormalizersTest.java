package br.com.minietl.transform;

import br.com.minietl.transform.normalizer.CidadeNormalizer;
import br.com.minietl.transform.normalizer.EmailNormalizer;
import br.com.minietl.transform.normalizer.NomeNormalizer;
import br.com.minietl.transform.normalizer.TelefoneNormalizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizersTest {

    private final NomeNormalizer nome = new NomeNormalizer();
    private final EmailNormalizer email = new EmailNormalizer();
    private final TelefoneNormalizer telefone = new TelefoneNormalizer();
    private final CidadeNormalizer cidade = new CidadeNormalizer();

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "  maria da silva  |Maria da Silva",
            "JOÃO   PEREIRA|João Pereira",
            "pedro henrique DOS santos|Pedro Henrique dos Santos",
            "josé d'ávila|José D'Ávila",
            "ana-clara e souza|Ana-Clara e Souza",
            "de souza|De Souza"
    })
    void padronizaNomes(String entrada, String esperado) {
        assertThat(nome.normalizar(entrada)).isEqualTo(esperado);
    }

    @Test
    void removeEspacosEspeciaisEInvisiveis() {
        assertThat(nome.normalizar("\u00A0ana\u200B costa\t")).isEqualTo("Ana Costa");
    }

    @Test
    void emailEmMinusculasSemEspacos() {
        assertThat(email.normalizar("  MARIA.Silva@Exemplo.COM ")).isEqualTo("maria.silva@exemplo.com");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "(31) 98765-4321|31987654321",
            "+55 11 3456-7890|1134567890",
            "55 (21) 99876-5432|21998765432",
            "61-99123-4567|61991234567",
            "31.3222.1111|3132221111",
            "123|123",
            "31 9abc|31 9abc"
    })
    void padronizaTelefones(String entrada, String esperado) {
        assertThat(telefone.normalizar(entrada)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "belo horizonte|Belo Horizonte",
            "  SÃO PAULO  |São Paulo",
            "rio de janeiro|Rio de Janeiro",
            "embu das artes|Embu das Artes"
    })
    void normalizaCidades(String entrada, String esperado) {
        assertThat(cidade.normalizar(entrada)).isEqualTo(esperado);
    }

    @Test
    void valoresNulosViramTextoVazio() {
        assertThat(nome.normalizar(null)).isEmpty();
        assertThat(email.normalizar(null)).isEmpty();
        assertThat(telefone.normalizar(null)).isEmpty();
        assertThat(cidade.normalizar("   ")).isEmpty();
    }
}
