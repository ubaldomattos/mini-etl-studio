package br.com.minietl.domain.repository;

import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RegistroProcessamentoRepository extends JpaRepository<RegistroProcessamento, Long> {

    Page<RegistroProcessamento> findByProcessamentoId(Long processamentoId, Pageable pageable);

    Page<RegistroProcessamento> findByProcessamentoIdAndSituacao(Long processamentoId, SituacaoRegistro situacao,
                                                                 Pageable pageable);

    List<RegistroProcessamento> findByProcessamentoIdAndSituacaoOrderByNumeroLinha(Long processamentoId,
                                                                                    SituacaoRegistro situacao);

    /** Leitura paginada (sem count) usada na geração dos CSVs. */
    Slice<RegistroProcessamento> findByProcessamentoIdAndSituacaoIn(Long processamentoId,
                                                                    Collection<SituacaoRegistro> situacoes,
                                                                    Pageable pageable);

    long countByProcessamentoId(Long processamentoId);
}
