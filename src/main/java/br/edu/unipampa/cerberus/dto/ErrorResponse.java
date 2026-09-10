package br.edu.unipampa.cerberus.dto;

import java.time.Instant;
import java.util.List;

/**
 * Corpo padronizado de erro retornado pela API.
 *
 * @param timestamp momento do erro
 * @param status    código HTTP
 * @param error     rótulo do erro
 * @param details   mensagens detalhadas (ex.: erros de validação por campo)
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        List<String> details) {

    public static ErrorResponse of(int status, String error, List<String> details) {
        return new ErrorResponse(Instant.now(), status, error, details);
    }
}
