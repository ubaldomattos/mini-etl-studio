package br.com.minietl.transform.rule;

import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.CamposPadronizados;
import br.com.minietl.transform.ResultadoRegra;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Telefone brasileiro já normalizado (apenas dígitos): DDD (11 a 99) mais 8 dígitos
 * (fixo, iniciando em 2 a 5) ou 9 dígitos (celular, iniciando em 9).
 */
@Component
@Order(4)
public class TelefoneFormatoRule implements ValidationRule {

    public static final String CODIGO = "TELEFONE_FORMATO";

    private static final Pattern FORMATO = Pattern.compile("^[1-9][1-9](9\\d{8}|[2-5]\\d{7})$");

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public ResultadoRegra validar(RegistroExtraido original, CamposPadronizados campos) {
        String telefone = campos.telefone();
        if (telefone.isEmpty()) {
            return ResultadoRegra.naoAplicavel(CODIGO);
        }
        return FORMATO.matcher(telefone).matches()
                ? ResultadoRegra.aprovada(CODIGO)
                : ResultadoRegra.reprovada(CODIGO,
                "Telefone inválido: '" + telefone + "' (esperado DDD + 8 ou 9 dígitos)");
    }
}
