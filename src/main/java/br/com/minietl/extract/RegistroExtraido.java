package br.com.minietl.extract;

/**
 * Registro lido do CSV, exatamente como veio no arquivo (sem trim ou qualquer ajuste).
 *
 * @param numeroLinha       número da linha física no arquivo (o cabeçalho é a linha 1)
 * @param nome              valor original; {@code null} se a coluna não existia na linha
 * @param linhaBruta        linha original reconstruída em CSV (preserva também colunas extras)
 * @param problemaEstrutura descrição do problema de estrutura da linha, ou {@code null}
 */
public record RegistroExtraido(
        int numeroLinha,
        String nome,
        String email,
        String telefone,
        String cidade,
        String linhaBruta,
        String problemaEstrutura) {

    public boolean temProblemaEstrutura() {
        return problemaEstrutura != null;
    }
}
