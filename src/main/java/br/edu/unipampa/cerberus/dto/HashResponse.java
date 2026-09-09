package br.edu.unipampa.cerberus.dto;

/**
 * Resultado do hashing de uma senha.
 *
 * @param algorithm algoritmo utilizado (ex.: bcrypt)
 * @param hash      hash resultante, no formato modular crypt (inclui o salt)
 */
public record HashResponse(String algorithm, String hash) {
}
