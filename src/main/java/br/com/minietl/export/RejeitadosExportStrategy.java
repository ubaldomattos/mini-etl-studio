package br.com.minietl.export;

import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Registros inválidos e duplicados, com os dados originais e o motivo da rejeição. */
@Component
public class RejeitadosExportStrategy implements ExportStrategy {

    static final String TIPO_VALIDACAO = "VALIDACAO";
    static final String TIPO_DUPLICIDADE = "DUPLICIDADE";

    @Override
    public TipoExportacao tipo() {
        return TipoExportacao.REJEITADOS;
    }

    @Override
    public Set<SituacaoRegistro> situacoes() {
        return Set.of(SituacaoRegistro.INVALIDO, SituacaoRegistro.DUPLICADO);
    }

    @Override
    public String[] cabecalho() {
        return new String[]{"numero_linha", "nome", "email", "telefone", "cidade", "tipo_rejeicao",
                "motivo_rejeicao"};
    }

    @Override
    public String[] linha(RegistroProcessamento r) {
        String tipo = r.getSituacao() == SituacaoRegistro.DUPLICADO ? TIPO_DUPLICIDADE : TIPO_VALIDACAO;
        return new String[]{String.valueOf(r.getNumeroLinha()), r.getOrigNome(), r.getOrigEmail(),
                r.getOrigTelefone(), r.getOrigCidade(), tipo, r.getMensagem()};
    }
}
