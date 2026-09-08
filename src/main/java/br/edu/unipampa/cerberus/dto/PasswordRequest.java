package br.edu.unipampa.cerberus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo de requisição que carrega uma senha a ser analisada.
 *
 * <p>A senha nunca é registrada em log nem persistida pelo serviço.</p>
 */
public record PasswordRequest(
        @Schema(description = "Senha em texto claro a ser avaliada", example = "S3nh@Muito!Forte2024")
        @NotBlank(message = "a senha não pode ser vazia")
        @Size(max = 256, message = "a senha não pode exceder 256 caracteres")
        String password) {
}
