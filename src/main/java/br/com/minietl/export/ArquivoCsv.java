package br.com.minietl.export;

/** Arquivo CSV gerado em memória para download. */
public record ArquivoCsv(String nomeArquivo, byte[] conteudo) {
}
