package br.com.minietl.transform.normalizer;

import org.springframework.stereotype.Component;

import java.util.Locale;

/** Remove espaços nas pontas e converte o e-mail para minúsculas. */
@Component
public class EmailNormalizer {

    public String normalizar(String email) {
        return TextoUtil.limpar(email).toLowerCase(Locale.ROOT);
    }
}
