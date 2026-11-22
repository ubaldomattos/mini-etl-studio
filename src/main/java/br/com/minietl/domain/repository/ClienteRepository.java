package br.com.minietl.domain.repository;

import br.com.minietl.domain.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    int TAMANHO_LOTE = 500;

    List<Cliente> findByEmailIn(Collection<String> emails);

    @Query("""
            select c from Cliente c
            where :termo = ''
               or lower(c.nome) like lower(concat('%', :termo, '%'))
               or lower(c.email) like lower(concat('%', :termo, '%'))
            """)
    Page<Cliente> buscar(@Param("termo") String termo, Pageable pageable);

    /** Retorna o id do cliente existente para cada e-mail informado (consulta em lotes). */
    default Map<String, Long> mapearIdsPorEmail(Collection<String> emails) {
        Map<String, Long> resultado = new HashMap<>();
        List<String> lista = emails.stream().distinct().toList();
        for (int i = 0; i < lista.size(); i += TAMANHO_LOTE) {
            List<String> lote = lista.subList(i, Math.min(i + TAMANHO_LOTE, lista.size()));
            findByEmailIn(lote).forEach(c -> resultado.put(c.getEmail(), c.getId()));
        }
        return resultado;
    }
}
