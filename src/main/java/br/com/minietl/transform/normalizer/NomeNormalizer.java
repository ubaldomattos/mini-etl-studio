package br.com.minietl.transform.normalizer;

import org.springframework.stereotype.Component;

/** Remove espaços desnecessários e padroniza o nome em Title Case. */
@Component
public class NomeNormalizer {

    public String normalizar(String nome) {
        return TextoUtil.tituloPtBr(nome);
    }
}
