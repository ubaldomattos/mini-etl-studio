package br.com.minietl.dto;

import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.enums.StatusProcessamento;

import java.time.LocalDateTime;

public record ProcessamentoDto(
        Long id,
        String nomeArquivo,
        LocalDateTime dataExecucao,
        StatusProcessamento status,
        int extraidos,
        int validos,
        int invalidos,
        int duplicados,
        int carregados) {

    public static ProcessamentoDto de(Processamento p) {
        return new ProcessamentoDto(p.getId(), p.getNomeArquivo(), p.getDataExecucao(), p.getStatus(),
                p.getQtdExtraidos(), p.getQtdValidos(), p.getQtdInvalidos(), p.getQtdDuplicados(),
                p.getQtdCarregados());
    }

    public boolean carregado() {
        return status == StatusProcessamento.CARREGADO;
    }
}