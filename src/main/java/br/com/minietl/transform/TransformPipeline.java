package br.com.minietl.transform;

import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.normalizer.CidadeNormalizer;
import br.com.minietl.transform.normalizer.EmailNormalizer;
import br.com.minietl.transform.normalizer.NomeNormalizer;
import br.com.minietl.transform.normalizer.TelefoneNormalizer;
import br.com.minietl.transform.rule.ValidationRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Etapa Transform (parte 1): padroniza os campos e aplica as regras de validação.
 * A detecção de duplicidades é feita depois, por {@link DuplicateDetector}.
 */
@Component
public class TransformPipeline {

    private final NomeNormalizer nomeNormalizer;
    private final EmailNormalizer emailNormalizer;
    private final TelefoneNormalizer telefoneNormalizer;
    private final CidadeNormalizer cidadeNormalizer;
    private final List<ValidationRule> regras;

    public TransformPipeline(NomeNormalizer nomeNormalizer, EmailNormalizer emailNormalizer,
                             TelefoneNormalizer telefoneNormalizer, CidadeNormalizer cidadeNormalizer,
                             List<ValidationRule> regras) {
        this.nomeNormalizer = nomeNormalizer;
        this.emailNormalizer = emailNormalizer;
        this.telefoneNormalizer = telefoneNormalizer;
        this.cidadeNormalizer = cidadeNormalizer;
        this.regras = List.copyOf(regras);
    }

    public List<RegistroAvaliado> avaliar(List<RegistroExtraido> registros) {
        return registros.stream().map(this::avaliar).toList();
    }

    public RegistroAvaliado avaliar(RegistroExtraido registro) {
        CamposPadronizados campos = new CamposPadronizados(
                nomeNormalizer.normalizar(registro.nome()),
                emailNormalizer.normalizar(registro.email()),
                telefoneNormalizer.normalizar(registro.telefone()),
                cidadeNormalizer.normalizar(registro.cidade()));

        List<ResultadoRegra> resultados = regras.stream().map(r -> r.validar(registro, campos)).toList();
        boolean reprovado = resultados.stream().anyMatch(ResultadoRegra::reprovada);
        return new RegistroAvaliado(registro, campos,
                reprovado ? SituacaoRegistro.INVALIDO : SituacaoRegistro.VALIDO, resultados, null);
    }
}
