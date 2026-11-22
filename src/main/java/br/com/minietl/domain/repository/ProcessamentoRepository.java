package br.com.minietl.domain.repository;

import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.dto.TotaisDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProcessamentoRepository extends JpaRepository<Processamento, Long> {

    Page<Processamento> findAllByOrderByDataExecucaoDescIdDesc(Pageable pageable);

    List<Processamento> findTop10ByOrderByDataExecucaoDescIdDesc();

    @Query("""
            select new br.com.minietl.dto.TotaisDto(
                coalesce(sum(p.qtdExtraidos), 0L), coalesce(sum(p.qtdValidos), 0L),
                coalesce(sum(p.qtdInvalidos), 0L), coalesce(sum(p.qtdDuplicados), 0L),
                coalesce(sum(p.qtdCarregados), 0L))
            from Processamento p
            """)
    TotaisDto somarTotais();
}
