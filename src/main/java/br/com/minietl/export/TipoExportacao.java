package br.com.minietl.export;

import br.com.minietl.exception.RecursoNaoEncontradoException;

import java.util.Arrays;

/** As três modalidades de exportação disponíveis. */
public enum TipoExportacao {
    TRATADO("tratado", "CSV tratado"),
    REJEITADOS("rejeitados", "CSV de rejeitados"),
    COMPLETO("completo", "CSV completo");

    private final String slug;
    private final String titulo;

    TipoExportacao(String slug, String titulo) {
        this.slug = slug;
        this.titulo = titulo;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitulo() {
        return titulo;
    }

    public static TipoExportacao deSlug(String slug) {
        return Arrays.stream(values()).filter(t -> t.slug.equalsIgnoreCase(slug)).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de exportação desconhecido: " + slug));
    }
}
