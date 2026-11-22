package br.com.minietl.domain.enums;

/** Estado do processamento no fluxo ETL. */
public enum StatusProcessamento {
    /** Extração e transformação concluídas; carga ainda não realizada. */
    PROCESSADO("Processado"),
    /** Registros válidos carregados no banco. */
    CARREGADO("Carregado");

    private final String descricao;

    StatusProcessamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
