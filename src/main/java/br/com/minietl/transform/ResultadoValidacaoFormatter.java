package br.com.minietl.transform;

import br.com.minietl.domain.enums.SituacaoRegistro;

import java.util.List;
import java.util.stream.Collectors;

/** Monta os textos de resultado de validação e de motivo de rejeição. */
public final class ResultadoValidacaoFormatter {

    static final String PREFIXO_VALIDO = "Todas as validações foram aprovadas. ";
    static final String PREFIXO_INVALIDO = "Validação reprovada. ";
    static final String PREFIXO_DUPLICADO = "Duplicidade identificada. ";
    public static final String CODIGO_DUPLICIDADE = "DUPLICIDADE";

    private ResultadoValidacaoFormatter() {
    }

    /** Texto com a descrição de cada validação e seu resultado. */
    public static String resultado(SituacaoRegistro situacao, List<ResultadoRegra> resultados) {
        String prefixo = switch (situacao) {
            case VALIDO -> PREFIXO_VALIDO;
            case INVALIDO -> PREFIXO_INVALIDO;
            case DUPLICADO -> PREFIXO_DUPLICADO;
        };
        return prefixo + resultados.stream().map(ResultadoValidacaoFormatter::descrever)
                .collect(Collectors.joining("; "));
    }

    /** Motivos das reprovações, separados por "; ". Retorna {@code null} quando não há reprovação. */
    public static String motivos(List<ResultadoRegra> resultados) {
        String motivos = resultados.stream().filter(ResultadoRegra::reprovada)
                .map(ResultadoRegra::mensagem).collect(Collectors.joining("; "));
        return motivos.isEmpty() ? null : motivos;
    }

    /** Reescreve o resultado de um registro válido que se tornou duplicado de um cliente já na base. */
    public static String comoDuplicadoNaBase(String resultadoAnterior, String motivo) {
        String base = resultadoAnterior == null ? "" : resultadoAnterior;
        if (base.startsWith(PREFIXO_VALIDO)) {
            base = PREFIXO_DUPLICADO + base.substring(PREFIXO_VALIDO.length());
        }
        String aprovada = CODIGO_DUPLICIDADE + "=APROVADA";
        String reprovada = CODIGO_DUPLICIDADE + "=REPROVADA (" + motivo + ")";
        return base.contains(aprovada) ? base.replace(aprovada, reprovada) : base + "; " + reprovada;
    }

    private static String descrever(ResultadoRegra r) {
        return r.status() == ResultadoRegra.Status.REPROVADA
                ? r.codigo() + "=REPROVADA (" + r.mensagem() + ")"
                : r.codigo() + "=" + r.status();
    }
}
