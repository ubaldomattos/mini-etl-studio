package br.com.minietl.domain.enums;

/** Classificação de um registro após extração, transformação e validação. */
public enum SituacaoRegistro {
    VALIDO("Válido"),
    INVALIDO("Inválido"),
    DUPLICADO("Duplicado");

    private final String descricao;

    SituacaoRegistro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
