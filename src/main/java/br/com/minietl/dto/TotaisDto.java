package br.com.minietl.dto;

/** Totais acumulados de todos os processamentos. */
public record TotaisDto(long extraidos, long validos, long invalidos, long duplicados, long carregados) {
}
