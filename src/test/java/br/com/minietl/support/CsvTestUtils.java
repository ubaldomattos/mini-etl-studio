package br.com.minietl.support;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180ParserBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvTestUtils {

    private CsvTestUtils() {
    }

    /** Lê um arquivo de src/test/resources/csv. */
    public static byte[] recurso(String nome) {
        try (InputStream in = CsvTestUtils.class.getResourceAsStream("/csv/" + nome)) {
            if (in == null) {
                throw new IllegalArgumentException("Recurso não encontrado: " + nome);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] utf8(String conteudo) {
        return conteudo.getBytes(StandardCharsets.UTF_8);
    }

    /** Converte o CSV exportado (com BOM) em linhas, usando um parser de CSV real. */
    public static List<String[]> lerExportado(byte[] conteudo) {
        String texto = new String(conteudo, StandardCharsets.UTF_8);
        if (texto.startsWith("\uFEFF")) {
            texto = texto.substring(1);
        }
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(texto))
                .withCSVParser(new RFC4180ParserBuilder().withSeparator(',').build()).build()) {
            return reader.readAll();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
