package br.edu.unipampa.cerberus.dto;

import java.util.List;

/**
 * Resultado da avaliação de uma senha contra a política configurada.
 *
 * @param compliant  true se a senha atende a todos os requisitos da política
 * @param violations lista de requisitos não atendidos (vazia quando compliant)
 * @param strength   detalhamento da análise de força
 * @param breach     detalhamento da verificação de vazamento
 */
public record PolicyResponse(
        boolean compliant,
        List<String> violations,
        StrengthResponse strength,
        BreachResponse breach) {
}
