package br.com.minietl.extract;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvValidationException;
import br.com.minietl.exception.ArquivoInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Etapa Extract: lê o CSV, valida o layout (cabeçalho) e converte as linhas
 * para {@link RegistroExtraido}, sem alterar nenhum valor.
 */
@Component
public class CsvExtractor {

    private static final Logger log = LoggerFactory.getLogger(CsvExtractor.class);

    private static final List<String> COLUNAS_OBRIGATORIAS = List.of("nome", "email", "telefone", "cidade");
    private static final Map<String, String> ROTULOS = Map.of(
            "nome", "Nome", "email", "E-mail", "telefone", "Telefone", "cidade", "Cidade");

    public ExtracaoResultado extrair(byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new ArquivoInvalidoException("O arquivo está vazio.");
        }
        for (byte b : conteudo) {
            if (b == 0) {
                throw new ArquivoInvalidoException("O arquivo não parece ser um CSV de texto (contém dados binários).");
            }
        }

        List<String> avisos = new ArrayList<>();
        String texto = decodificar(conteudo, avisos);
        char delimitador = detectarDelimitador(texto);

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(texto))
                .withCSVParser(new RFC4180ParserBuilder().withSeparator(delimitador).build())
                .build()) {

            String[] cabecalho = lerCabecalho(reader);
            int[] indices = mapearColunas(cabecalho);
            registrarColunasExtras(cabecalho, indices, avisos);

            List<RegistroExtraido> registros = new ArrayList<>();
            int emBranco = 0;
            String[] campos;
            while (true) {
                int linhaInicial = (int) reader.getLinesRead() + 1;
                campos = reader.readNext();
                if (campos == null) {
                    break;
                }
                if (estaEmBranco(campos)) {
                    emBranco++;
                    continue;
                }
                registros.add(montarRegistro(linhaInicial, campos, cabecalho.length, indices, delimitador));
            }

            if (registros.isEmpty()) {
                throw new ArquivoInvalidoException("O arquivo possui cabeçalho, mas nenhum registro de dados.");
            }
            if (emBranco > 0) {
                avisos.add(emBranco + " linha(s) em branco ignorada(s).");
            }
            log.info("Extração concluída: {} registro(s), delimitador '{}'", registros.size(), delimitador);
            return new ExtracaoResultado(delimitador, List.of(cabecalho), List.copyOf(avisos),
                    List.copyOf(registros), emBranco);
        } catch (IOException | CsvValidationException | RuntimeException e) {
            if (e instanceof ArquivoInvalidoException invalido) {
                throw invalido;
            }
            throw new ArquivoInvalidoException("Não foi possível ler o CSV: estrutura malformada ("
                    + e.getMessage() + ").", e);
        }
    }

    private String decodificar(byte[] conteudo, List<String> avisos) {
        int inicio = temBomUtf8(conteudo) ? 3 : 0;
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(conteudo, inicio, conteudo.length - inicio))
                    .toString();
        } catch (CharacterCodingException e) {
            avisos.add("O arquivo não está em UTF-8; foi interpretado como Windows-1252.");
            return new String(conteudo, inicio, conteudo.length - inicio, Charset.forName("windows-1252"));
        }
    }

    private boolean temBomUtf8(byte[] c) {
        return c.length >= 3 && (c[0] & 0xFF) == 0xEF && (c[1] & 0xFF) == 0xBB && (c[2] & 0xFF) == 0xBF;
    }

    /** Escolhe o delimitador mais frequente (fora de aspas) na primeira linha não vazia. */
    char detectarDelimitador(String texto) {
        int virgulas = 0;
        int pontoVirgulas = 0;
        int tabs = 0;
        boolean dentroAspas = false;
        boolean viuConteudo = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"') {
                dentroAspas = !dentroAspas;
            } else if (!dentroAspas) {
                if (c == '\n' || c == '\r') {
                    if (viuConteudo) {
                        break;
                    }
                    continue;
                }
                viuConteudo = true;
                if (c == ',') {
                    virgulas++;
                } else if (c == ';') {
                    pontoVirgulas++;
                } else if (c == '\t') {
                    tabs++;
                }
            }
        }
        if (pontoVirgulas > virgulas && pontoVirgulas >= tabs) {
            return ';';
        }
        if (tabs > virgulas && tabs > pontoVirgulas) {
            return '\t';
        }
        return ',';
    }

    private String[] lerCabecalho(CSVReader reader) throws IOException, CsvValidationException {
        String[] linha;
        while ((linha = reader.readNext()) != null) {
            if (!estaEmBranco(linha)) {
                return linha;
            }
        }
        throw new ArquivoInvalidoException("O arquivo não possui cabeçalho nem registros.");
    }

    /** Retorna, para cada coluna obrigatória (na ordem nome, email, telefone, cidade), o índice no arquivo. */
    private int[] mapearColunas(String[] cabecalho) {
        int[] indices = new int[COLUNAS_OBRIGATORIAS.size()];
        List<String> ausentes = new ArrayList<>();
        for (int i = 0; i < COLUNAS_OBRIGATORIAS.size(); i++) {
            String chave = COLUNAS_OBRIGATORIAS.get(i);
            indices[i] = -1;
            for (int c = 0; c < cabecalho.length; c++) {
                if (chaveDe(cabecalho[c]).equals(chave)) {
                    indices[i] = c;
                    break;
                }
            }
            if (indices[i] < 0) {
                ausentes.add(ROTULOS.get(chave));
            }
        }
        if (!ausentes.isEmpty()) {
            throw new ArquivoInvalidoException("Colunas obrigatórias ausentes: " + String.join(", ", ausentes)
                    + ". Colunas encontradas: " + String.join(", ", cabecalho) + ".");
        }
        return indices;
    }

    private void registrarColunasExtras(String[] cabecalho, int[] indices, List<String> avisos) {
        List<String> extras = new ArrayList<>();
        for (int c = 0; c < cabecalho.length; c++) {
            boolean usada = false;
            for (int indice : indices) {
                usada |= indice == c;
            }
            if (!usada && !cabecalho[c].isBlank()) {
                extras.add(cabecalho[c].trim());
            }
        }
        if (!extras.isEmpty()) {
            avisos.add("Coluna(s) extra(s) ignorada(s): " + String.join(", ", extras) + ".");
        }
    }

    /** Normaliza um cabeçalho: sem acentos, minúsculo, apenas letras e dígitos ("E-mail" vira "email"). */
    static String chaveDe(String cabecalho) {
        String semAcento = Normalizer.normalize(cabecalho, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private RegistroExtraido montarRegistro(int linha, String[] campos, int colunasEsperadas, int[] indices,
                                            char delimitador) {
        String problema = campos.length == colunasEsperadas ? null
                : "Estrutura inconsistente: esperado(s) " + colunasEsperadas + " coluna(s), encontrado(s) "
                + campos.length;
        return new RegistroExtraido(linha,
                valor(campos, indices[0]), valor(campos, indices[1]),
                valor(campos, indices[2]), valor(campos, indices[3]),
                reconstruirLinha(campos, delimitador), problema);
    }

    private String valor(String[] campos, int indice) {
        return indice < campos.length ? campos[indice] : null;
    }

    private boolean estaEmBranco(String[] campos) {
        for (String campo : campos) {
            if (campo != null && !campo.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String reconstruirLinha(String[] campos, char delimitador) {
        StringWriter saida = new StringWriter();
        try (ICSVWriter writer = new CSVWriterBuilder(saida).withSeparator(delimitador).withLineEnd("").build()) {
            writer.writeNext(campos, false);
        } catch (IOException e) {
            return String.join(String.valueOf(delimitador), campos);
        }
        return saida.toString();
    }
}
