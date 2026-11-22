package br.com.minietl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configura o DataSource do SQLite. O arquivo (e a pasta) são criados na primeira execução.
 *
 * <p>O SQLite aceita um único escritor por vez; por isso o pool tem apenas uma conexão,
 * com WAL e {@code busy_timeout} para tolerar acessos concorrentes.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(AppProperties properties) {
        Path arquivo = Path.of(properties.dbPath()).toAbsolutePath().normalize();
        criarDiretorio(arquivo);
        log.info("Banco SQLite em: {}", arquivo);

        HikariConfig config = new HikariConfig();
        config.setPoolName("sqlite-pool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + arquivo + "?foreign_keys=on&busy_timeout=5000&journal_mode=WAL");
        config.setMaximumPoolSize(1);
        return new HikariDataSource(config);
    }

    private void criarDiretorio(Path arquivo) {
        Path pasta = arquivo.getParent();
        if (pasta == null) {
            return;
        }
        try {
            Files.createDirectories(pasta);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar a pasta do banco de dados: " + pasta, e);
        }
    }
}
