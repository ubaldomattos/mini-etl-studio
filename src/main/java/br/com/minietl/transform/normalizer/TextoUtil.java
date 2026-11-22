package br.com.minietl.transform.normalizer;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Operações de texto compartilhadas pelos normalizadores. */
final class TextoUtil {

    private static final Pattern INVISIVEIS = Pattern.compile("[\\u200B\\u200C\\u200D\\uFEFF]");
    private static final Pattern ESPACOS = Pattern.compile("(?U)\\s+");
    private static final Set<String> PARTICULAS = Set.of("da", "de", "do", "das", "dos", "e", "di", "du");

    private TextoUtil() {
    }

    /** Remove espaços (inclusive NBSP) nas pontas, caracteres invisíveis e espaços repetidos. */
    static String limpar(String valor) {
        if (valor == null) {
            return "";
        }
        String semInvisiveis = INVISIVEIS.matcher(valor).replaceAll("");
        return ESPACOS.matcher(semInvisiveis).replaceAll(" ").strip();
    }

    /** Title Case em pt-BR: "JOÃO DA SILVA" vira "João da Silva". */
    static String tituloPtBr(String valor) {
        String limpo = limpar(valor);
        if (limpo.isEmpty()) {
            return limpo;
        }
        String[] palavras = limpo.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder saida = new StringBuilder();
        for (int i = 0; i < palavras.length; i++) {
            if (i > 0) {
                saida.append(' ');
            }
            String palavra = palavras[i];
            saida.append(i > 0 && PARTICULAS.contains(palavra) ? palavra : capitalizar(palavra));
        }
        return saida.toString();
    }

    /**
     * Capitaliza a primeira letra da palavra e as letras após qualquer caractere que não seja
     * letra (hífen, apóstrofo, aspas): "d'ávila" vira "D'Ávila", "ana-clara" vira "Ana-Clara".
     */
    private static String capitalizar(String palavra) {
        StringBuilder saida = new StringBuilder(palavra.length());
        boolean inicio = true;
        for (int i = 0; i < palavra.length(); i++) {
            char c = palavra.charAt(i);
            saida.append(inicio ? Character.toUpperCase(c) : c);
            inicio = !Character.isLetter(c);
        }
        return saida.toString();
    }
}
