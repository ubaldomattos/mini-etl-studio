package br.com.minietl.domain;

import br.com.minietl.MiniEtlStudioApplication;
import br.com.minietl.domain.repository.ClienteRepository;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.export.CsvExportService;
import br.com.minietl.export.TipoExportacao;
import br.com.minietl.load.CargaService;
import br.com.minietl.service.ProcessamentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;

import static br.com.minietl.support.CsvTestUtils.lerExportado;
import static br.com.minietl.support.CsvTestUtils.recurso;
import static org.assertj.core.api.Assertions.assertThat;

/** Os dados (clientes e histórico) permanecem após reiniciar a aplicação com o mesmo arquivo SQLite. */
class PersistenciaAposReinicioTest {

    @TempDir
    Path pasta;

    @Test
    void dadosEExportacoesSobrevivemAReinicializacao() {
        String dbPath = pasta.resolve("sub/reinicio.db").toString();
        Long processamentoId;

        try (ConfigurableApplicationContext ctx = iniciar(dbPath)) {
            processamentoId = ctx.getBean(ProcessamentoService.class)
                    .processar("misto.csv", recurso("misto.csv")).getId();
            ctx.getBean(CargaService.class).carregar(processamentoId);
        }

        assertThat(Path.of(dbPath)).exists();

        try (ConfigurableApplicationContext ctx = iniciar(dbPath)) {
            assertThat(ctx.getBean(ClienteRepository.class).count()).isEqualTo(3);
            assertThat(ctx.getBean(ProcessamentoRepository.class).findById(processamentoId)).isPresent();

            CsvExportService export = ctx.getBean(CsvExportService.class);
            assertThat(lerExportado(export.exportar(processamentoId, TipoExportacao.COMPLETO).conteudo()))
                    .hasSize(1 + 7);
            assertThat(lerExportado(export.exportar(processamentoId, TipoExportacao.REJEITADOS).conteudo()))
                    .hasSize(1 + 4);
        }
    }

    private ConfigurableApplicationContext iniciar(String dbPath) {
        return new SpringApplicationBuilder(MiniEtlStudioApplication.class)
                .web(WebApplicationType.NONE)
                // Argumentos de linha de comando têm precedência sobre o application.yml.
                .run("--app.db-path=" + dbPath, "--logging.level.root=WARN", "--spring.main.banner-mode=off");
    }
}
