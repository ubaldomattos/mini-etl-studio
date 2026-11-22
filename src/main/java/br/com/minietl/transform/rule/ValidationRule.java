package br.com.minietl.transform.rule;

import br.com.minietl.extract.RegistroExtraido;
import br.com.minietl.transform.CamposPadronizados;
import br.com.minietl.transform.ResultadoRegra;

/** Regra de validação de um registro. Novas regras são adicionadas como novos componentes. */
public interface ValidationRule {

    String codigo();

    ResultadoRegra validar(RegistroExtraido original, CamposPadronizados campos);
}
