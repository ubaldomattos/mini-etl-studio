package br.com.minietl.dto;

/** Linha exibida na prévia (dados originais, sem transformação). */
public record LinhaPreviaDto(int numeroLinha, String nome, String email, String telefone, String cidade,
                             String problemaEstrutura) {
}