package br.com.minietl.service;

import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.repository.ClienteRepository;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.dto.DashboardDto;
import br.com.minietl.dto.GraficosDto;
import br.com.minietl.dto.ProcessamentoDto;
import br.com.minietl.dto.TotaisDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Monta os indicadores e os dados dos gráficos do dashboard. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProcessamentoRepository processamentoRepository;
    private final ClienteRepository clienteRepository;

    public DashboardService(ProcessamentoRepository processamentoRepository, ClienteRepository clienteRepository) {
        this.processamentoRepository = processamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    public DashboardDto montar() {
        TotaisDto totais = processamentoRepository.somarTotais();
        List<Processamento> recentes = processamentoRepository.findTop10ByOrderByDataExecucaoDescIdDesc();
        List<ProcessamentoDto> dtos = recentes.stream().map(ProcessamentoDto::de).toList();
        ProcessamentoDto ultimo = dtos.isEmpty() ? null : dtos.get(0);
        return new DashboardDto(totais, clienteRepository.count(), ultimo, dtos, montarGraficos(totais, dtos));
    }

    private GraficosDto montarGraficos(TotaisDto totais, List<ProcessamentoDto> recentes) {
        // Ordem cronológica (do mais antigo para o mais recente) no gráfico de barras.
        List<ProcessamentoDto> ordenados = new ArrayList<>(recentes);
        Collections.reverse(ordenados);
        return new GraficosDto(
                ordenados.stream().map(p -> "#" + p.id()).toList(),
                ordenados.stream().map(ProcessamentoDto::validos).toList(),
                ordenados.stream().map(ProcessamentoDto::invalidos).toList(),
                ordenados.stream().map(ProcessamentoDto::duplicados).toList(),
                List.of(totais.validos(), totais.invalidos(), totais.duplicados()));
    }
}
