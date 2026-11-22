package br.com.minietl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configurações externalizadas da aplicação (prefixo {@code app}).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("./data/mini-etl.db") String dbPath,
        @DefaultValue Preview preview,
        @DefaultValue Export export) {

    public record Preview(@DefaultValue("50") int linhas) {
    }

    public record Export(@DefaultValue(",") char delimiter) {
    }
}
