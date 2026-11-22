package br.com.minietl.export;

import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Somente registros válidos, com os dados já transformados (os mesmos elegíveis para carga). */
@Component
public class TratadoExportStrategy implements ExportStrategy {

    @Override
    public TipoExportacao tipo() {
        return TipoExportacao.TRATADO;
    }

    @Override
    public Set<SituacaoRegistro> situacoes() {
        return Set.of(SituacaoRegistro.VALIDO);
    }

    @Override
    public String[] cabecalho() {
        return new String[]{"nome", "email", "telefone", "cidade"};
    }

    @Override
    public String[] linha(RegistroProcessamento r) {
        return new String[]{r.getTransNome(), r.getTransEmail(), r.getTransTelefone(), r.getTransCidade()};
    }
}
