package br.com.minietl.web;

import br.com.minietl.export.ArquivoCsv;
import br.com.minietl.export.CsvExportService;
import br.com.minietl.export.TipoExportacao;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

/** Download dos três CSVs de um processamento (recente ou do histórico). */
@Controller
public class ExportController {

    private static final MediaType CSV_UTF8 = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final CsvExportService exportService;

    public ExportController(CsvExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/processamentos/{id}/exportacao/{tipo}")
    public ResponseEntity<byte[]> exportar(@PathVariable Long id, @PathVariable String tipo) {
        ArquivoCsv arquivo = exportService.exportar(id, TipoExportacao.deSlug(tipo));
        return ResponseEntity.ok()
                .contentType(CSV_UTF8)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(arquivo.nomeArquivo()).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(arquivo.conteudo());
    }
}
