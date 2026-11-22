package br.com.minietl.transform.normalizer;

import org.springframework.stereotype.Component;

/** Normaliza o nome da cidade (espaços e Title Case). */
@Component
public class CidadeNormalizer {

    public String normalizar(String cidade) {
        return TextoUtil.tituloPtBr(cidade);
    }
}
