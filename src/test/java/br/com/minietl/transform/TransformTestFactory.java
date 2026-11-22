package br.com.minietl.transform;

import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.normalizer.CidadeNormalizer;
import br.com.minietl.transform.normalizer.EmailNormalizer;
import br.com.minietl.transform.normalizer.NomeNormalizer;
import br.com.minietl.transform.normalizer.TelefoneNormalizer;
import br.com.minietl.transform.rule.CamposObrigatoriosRule;
import br.com.minietl.transform.rule.EmailFormatoRule;
import br.com.minietl.transform.rule.EstruturaRule;
import br.com.minietl.transform.rule.TelefoneFormatoRule;

import java.util.List;

/** Monta o pipeline real sem Spring, para testes unitários. */
public final class TransformTestFactory {

    private TransformTestFactory() {
    }

    public static TransformPipeline pipeline() {
        return new TransformPipeline(new NomeNormalizer(), new EmailNormalizer(), new TelefoneNormalizer(),
                new CidadeNormalizer(), List.of(new EstruturaRule(), new CamposObrigatoriosRule(),
                new EmailFormatoRule(), new TelefoneFormatoRule()));
    }

    public static RegistroExtraido registro(int linha, String nome, String email, String telefone, String cidade) {
        return new RegistroExtraido(linha, nome, email, telefone, cidade,
                String.join(",", String.valueOf(nome), String.valueOf(email), String.valueOf(telefone),
                        String.valueOf(cidade)), null);
    }
}
