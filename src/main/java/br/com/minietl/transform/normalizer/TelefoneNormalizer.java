package br.com.minietl.transform.normalizer;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Padroniza telefones mantendo apenas dígitos e removendo o DDI 55 quando presente.
 *
 * <p>Se o valor contiver caracteres além dos usuais de formatação (dígitos, espaço, parênteses,
 * hífen, ponto e "+"), ele é devolvido apenas limpo (sem remover letras), para que a regra de
 * validação o reprove com o valor visível.
 */
@Component
public class TelefoneNormalizer {

    private static final Pattern SOMENTE_FORMATACAO = Pattern.compile("[0-9\\s().+\\-]*");

    public String normalizar(String telefone) {
        String limpo = TextoUtil.limpar(telefone);
        if (limpo.isEmpty() || !SOMENTE_FORMATACAO.matcher(limpo).matches()) {
            return limpo;
        }
        String digitos = limpo.replaceAll("\\D", "");
        boolean comDdi = (digitos.length() == 12 || digitos.length() == 13) && digitos.startsWith("55");
        return comDdi ? digitos.substring(2) : digitos;
    }
}
