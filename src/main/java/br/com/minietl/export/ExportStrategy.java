package br.com.minietl.export;

import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;

import java.util.Set;

/** Define o conteúdo de uma modalidade de exportação: quais registros e quais colunas. */
public interface ExportStrategy {

    TipoExportacao tipo();

    /** Situações incluídas no arquivo. */
    Set<SituacaoRegistro> situacoes();

    String[] cabecalho();

    String[] linha(RegistroProcessamento registro);
}
