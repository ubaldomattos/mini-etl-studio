package br.com.minietl.transform;

import br.com.minietl.domain.enums.SituacaoRegistro;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.com.minietl.transform.ResultadoValidacaoFormatter.CODIGO_DUPLICIDADE;

/**
 * Etapa Transform (parte 2): identifica duplicidades pelo e-mail padronizado.
 *
 * <p>Regras: apenas registros válidos participam; o primeiro registro válido com um e-mail é
 * mantido e os seguintes viram DUPLICADO. E-mails já existentes na base também geram DUPLICADO.
 */
@Component
public class DuplicateDetector {

    /**
     * @param avaliados   registros após validação, na ordem do arquivo
     * @param emailsNaBase e-mails já cadastrados, com o id do cliente correspondente
     */
    public List<RegistroAvaliado> aplicar(List<RegistroAvaliado> avaliados, Map<String, Long> emailsNaBase) {
        Map<String, Integer> primeiraLinhaPorEmail = new HashMap<>();
        List<RegistroAvaliado> resultado = new ArrayList<>(avaliados.size());

        for (RegistroAvaliado registro : avaliados) {
            if (registro.situacao() != SituacaoRegistro.VALIDO) {
                resultado.add(registro.comResultadoDuplicidade(ResultadoRegra.naoAplicavel(CODIGO_DUPLICIDADE), null));
                continue;
            }
            String email = registro.campos().email();
            Integer linhaOriginal = primeiraLinhaPorEmail.get(email);
            if (linhaOriginal != null) {
                String motivo = "E-mail duplicado: já informado na linha " + linhaOriginal;
                resultado.add(registro.comResultadoDuplicidade(
                        ResultadoRegra.reprovada(CODIGO_DUPLICIDADE, motivo), linhaOriginal));
            } else if (emailsNaBase.containsKey(email)) {
                String motivo = "E-mail já cadastrado na base de dados (cliente #" + emailsNaBase.get(email) + ")";
                resultado.add(registro.comResultadoDuplicidade(
                        ResultadoRegra.reprovada(CODIGO_DUPLICIDADE, motivo), null));
            } else {
                primeiraLinhaPorEmail.put(email, registro.original().numeroLinha());
                resultado.add(registro.comResultadoDuplicidade(ResultadoRegra.aprovada(CODIGO_DUPLICIDADE), null));
            }
        }
        return resultado;
    }
}
