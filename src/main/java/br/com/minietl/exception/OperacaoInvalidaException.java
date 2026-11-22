package br.com.minietl.exception;

/** A operação não é permitida no estado atual (ex.: carregar um processamento já carregado). */
public class OperacaoInvalidaException extends RuntimeException {

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
