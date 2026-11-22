package br.com.minietl.exception;

/** Falha na etapa de carga. A transação é revertida; nenhum registro é gravado. */
public class CargaException extends RuntimeException {

    public CargaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
