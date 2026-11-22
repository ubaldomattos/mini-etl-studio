package br.com.minietl.dto;

import java.util.List;

/** Prévia do arquivo antes do processamento. */
public record PreviaDto(
        String nomeArquivo,
        String delimitador,
        List<String> cabecalhos,
        List<String> avisos,
        int totalRegistros,
        long comProblemaEstrutura,
        int limite,
        List<LinhaPreviaDto> linhas) {

    public boolean truncada() {
        return totalRegistros > linhas.size();
    }
}