package br.com.minietl.exception;

/** O arquivo enviado não pode ser usado (vazio, formato ou estrutura inválidos). */
public class ArquivoInvalidoException extends RuntimeException {

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }

    public ArquivoInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
