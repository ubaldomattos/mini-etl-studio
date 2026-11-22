package br.com.minietl.transform;

/**
 * Resultado da avaliação de uma regra de validação sobre um registro.
 *
 * @param codigo   identificador da regra (ex.: EMAIL_FORMATO)
 * @param status   aprovada, reprovada ou não aplicável
 * @param mensagem motivo da reprovação (nulo quando aprovada)
 */
public record ResultadoRegra(String codigo, Status status, String mensagem) {

    public enum Status { APROVADA, REPROVADA, NAO_APLICAVEL }

    public static ResultadoRegra aprovada(String codigo) {
        return new ResultadoRegra(codigo, Status.APROVADA, null);
    }

    public static ResultadoRegra reprovada(String codigo, String mensagem) {
        return new ResultadoRegra(codigo, Status.REPROVADA, mensagem);
    }

    public static ResultadoRegra naoAplicavel(String codigo) {
        return new ResultadoRegra(codigo, Status.NAO_APLICAVEL, null);
    }

    public boolean reprovada() {
        return status == Status.REPROVADA;
    }
}
