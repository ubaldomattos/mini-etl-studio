package br.com.minietl.dto;

import java.util.List;

public record DashboardDto(
        TotaisDto totais,
        long totalClientes,
        ProcessamentoDto ultimo,
        List<ProcessamentoDto> recentes,
        GraficosDto graficos) {
}