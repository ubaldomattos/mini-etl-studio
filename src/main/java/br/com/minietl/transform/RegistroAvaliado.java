package br.com.minietl.transform;

import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.extract.RegistroExtraido;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro após a etapa Transform: dados originais, dados padronizados, situação e
 * resultado de cada regra.
 *
 * @param duplicadoDaLinha linha do primeiro registro com o mesmo e-mail, quando duplicado no arquivo
 */
public record RegistroAvaliado(
        RegistroExtraido original,
        CamposPadronizados campos,
        SituacaoRegistro situacao,
        List<ResultadoRegra> resultados,
        Integer duplicadoDaLinha) {

    public String resultadoValidacao() {
        return ResultadoValidacaoFormatter.resultado(situacao, resultados);
    }

    /** Motivos da rejeição; {@code null} para registros válidos. */
    public String mensagem() {
        return ResultadoValidacaoFormatter.motivos(resultados);
    }

    public RegistroAvaliado comResultadoDuplicidade(ResultadoRegra resultado, Integer linhaOriginal) {
        List<ResultadoRegra> novos = new ArrayList<>(resultados);
        novos.add(resultado);
        SituacaoRegistro novaSituacao = resultado.reprovada() ? SituacaoRegistro.DUPLICADO : situacao;
        return new RegistroAvaliado(original, campos, novaSituacao, List.copyOf(novos), linhaOriginal);
    }
}
