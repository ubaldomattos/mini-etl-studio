package br.com.minietl.transform;

/** Campos após as regras de transformação. Valores ausentes são representados por texto vazio. */
public record CamposPadronizados(String nome, String email, String telefone, String cidade) {
}
