package br.edu.unipampa.cerberus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Requisição para verificar se uma senha corresponde a um hash previamente gerado.
 */
public record VerifyRequest(
        @Schema(description = "Senha em texto claro", example = "S3nh@Muito!Forte2024")
        @NotBlank(message = "a senha não pode ser vazia")
        String password,

        @Schema(description = "Hash BCrypt a ser comparado",
                example = "$2a$12$abcdefghijklmnopqrstuv...")
        @NotBlank(message = "o hash não pode ser vazio")
        String hash) {
}
