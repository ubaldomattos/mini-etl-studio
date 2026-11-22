package br.com.minietl.transform.rule;

import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.CamposPadronizados;
import br.com.minietl.transform.ResultadoRegra;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/** Valida o formato do e-mail (já normalizado em minúsculas). Só se aplica quando há e-mail. */
@Component
@Order(3)
public class EmailFormatoRule implements ValidationRule {

    public static final String CODIGO = "EMAIL_FORMATO";

    private static final int TAMANHO_MAXIMO = 254;
    private static final Pattern FORMATO = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)+$");

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public ResultadoRegra validar(RegistroExtraido original, CamposPadronizados campos) {
        String email = campos.email();
        if (email.isEmpty()) {
            return ResultadoRegra.naoAplicavel(CODIGO);
        }
        boolean valido = email.length() <= TAMANHO_MAXIMO && FORMATO.matcher(email).matches();
        return valido
                ? ResultadoRegra.aprovada(CODIGO)
                : ResultadoRegra.reprovada(CODIGO, "E-mail inválido: '" + email + "'");
    }
}
