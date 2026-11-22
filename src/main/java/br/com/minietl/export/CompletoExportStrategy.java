package br.com.minietl.export;

import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Todos os registros extraídos, com os dados originais, a situação e o resultado das validações. */
@Component
public class CompletoExportStrategy implements ExportStrategy {

    @Override
    public TipoExportacao tipo() {
        return TipoExportacao.COMPLETO;
    }

    @Override
    public Set<SituacaoRegistro> situacoes() {
        return Set.of(SituacaoRegistro.VALIDO, SituacaoRegistro.INVALIDO, SituacaoRegistro.DUPLICADO);
    }

    @Override
    public String[] cabecalho() {
        return new String[]{"numero_linha", "nome", "email", "telefone", "cidade", "situacao",
                "resultado_validacao"};
    }

    @Override
    public String[] linha(RegistroProcessamento r) {
        return new String[]{String.valueOf(r.getNumeroLinha()), r.getOrigNome(), r.getOrigEmail(),
                r.getOrigTelefone(), r.getOrigCidade(), r.getSituacao().name(), r.getResultadoValidacao()};
    }
}
