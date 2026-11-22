package br.com.minietl.extract;

import java.util.List;

/**
 * Resultado da etapa Extract.
 *
 * @param delimitador         delimitador detectado no arquivo
 * @param cabecalhos          cabeçalhos exatamente como aparecem no arquivo
 * @param avisos              observações não bloqueantes (colunas extras, codificação, linhas em branco)
 * @param registros           registros extraídos, em ordem
 * @param linhasEmBrancoIgnoradas quantidade de linhas totalmente vazias descartadas
 */
public record ExtracaoResultado(
        char delimitador,
        List<String> cabecalhos,
        List<String> avisos,
        List<RegistroExtraido> registros,
        int linhasEmBrancoIgnoradas) {

    public long quantidadeComProblemaEstrutura() {
        return registros.stream().filter(RegistroExtraido::temProblemaEstrutura).count();
    }
}
