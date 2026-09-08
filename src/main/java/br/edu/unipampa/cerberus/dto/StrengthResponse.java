package br.edu.unipampa.cerberus.dto;

import java.util.List;

/**
 * Resultado da análise de força de uma senha.
 *
 * @param score        pontuação de 0 (muito fraca) a 4 (muito forte)
 * @param rating       rótulo textual correspondente ao score
 * @param entropyBits  estimativa de entropia em bits
 * @param length       comprimento da senha
 * @param hasLowercase se contém letras minúsculas
 * @param hasUppercase se contém letras maiúsculas
 * @param hasDigit     se contém dígitos
 * @param hasSymbol    se contém símbolos
 * @param warnings     problemas encontrados (ex.: senha comum, sequência)
 * @param suggestions  sugestões de melhoria
 */
public record StrengthResponse(
        int score,
        String rating,
        double entropyBits,
        int length,
        boolean hasLowercase,
        boolean hasUppercase,
        boolean hasDigit,
        boolean hasSymbol,
        List<String> warnings,
        List<String> suggestions) {
}
