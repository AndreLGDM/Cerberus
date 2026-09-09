package br.edu.unipampa.cerberus.dto;

/**
 * Resultado da verificação senha vs. hash.
 *
 * @param matches true se a senha corresponde ao hash informado
 */
public record VerifyResponse(boolean matches) {
}
