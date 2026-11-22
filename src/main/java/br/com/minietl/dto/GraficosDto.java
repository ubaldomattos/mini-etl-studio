package br.com.minietl.dto;

import java.util.List;

/** Dados dos gráficos do dashboard (serializados como JSON na página). */
public record GraficosDto(
        List<String> rotulos,
        List<Integer> validos,
        List<Integer> invalidos,
        List<Integer> duplicados,
        List<Long> distribuicao) {
}