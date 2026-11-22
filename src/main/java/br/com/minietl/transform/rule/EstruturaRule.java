package br.com.minietl.transform.rule;

import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.CamposPadronizados;
import br.com.minietl.transform.ResultadoRegra;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** A linha deve ter a mesma quantidade de colunas do cabeçalho. */
@Component
@Order(1)
public class EstruturaRule implements ValidationRule {

    public static final String CODIGO = "ESTRUTURA";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public ResultadoRegra validar(RegistroExtraido original, CamposPadronizados campos) {
        return original.temProblemaEstrutura()
                ? ResultadoRegra.reprovada(CODIGO, original.problemaEstrutura())
                : ResultadoRegra.aprovada(CODIGO);
    }
}
