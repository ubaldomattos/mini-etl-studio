package br.com.minietl.transform.rule;

import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.CamposPadronizados;
import br.com.minietl.transform.ResultadoRegra;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Nome, e-mail, telefone e cidade são obrigatórios. */
@Component
@Order(2)
public class CamposObrigatoriosRule implements ValidationRule {

    public static final String CODIGO = "CAMPOS_OBRIGATORIOS";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public ResultadoRegra validar(RegistroExtraido original, CamposPadronizados campos) {
        List<String> ausentes = new ArrayList<>();
        if (campos.nome().isEmpty()) {
            ausentes.add("nome");
        }
        if (campos.email().isEmpty()) {
            ausentes.add("e-mail");
        }
        if (campos.telefone().isEmpty()) {
            ausentes.add("telefone");
        }
        if (campos.cidade().isEmpty()) {
            ausentes.add("cidade");
        }
        return ausentes.isEmpty()
                ? ResultadoRegra.aprovada(CODIGO)
                : ResultadoRegra.reprovada(CODIGO, "Campo(s) obrigatório(s) ausente(s): " + String.join(", ", ausentes));
    }
}
