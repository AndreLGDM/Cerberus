package br.edu.unipampa.cerberus.controller;

import br.edu.unipampa.cerberus.dto.HashResponse;
import br.edu.unipampa.cerberus.dto.PasswordRequest;
import br.edu.unipampa.cerberus.dto.VerifyRequest;
import br.edu.unipampa.cerberus.dto.VerifyResponse;
import br.edu.unipampa.cerberus.service.PasswordHashService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de hashing seguro de senhas (BCrypt).
 */
@RestController
@RequestMapping("/api/v1/hash")
@Tag(name = "Hash", description = "Geração e verificação de hashes BCrypt")
public class HashController {

    private final PasswordHashService hashService;

    public HashController(PasswordHashService hashService) {
        this.hashService = hashService;
    }

    @Operation(summary = "Gera um hash BCrypt (com salt aleatório) para a senha")
    @PostMapping("/bcrypt")
    public HashResponse hash(@Valid @RequestBody PasswordRequest request) {
        return hashService.hash(request.password());
    }

    @Operation(summary = "Verifica se uma senha corresponde a um hash BCrypt")
    @PostMapping("/verify")
    public VerifyResponse verify(@Valid @RequestBody VerifyRequest request) {
        return new VerifyResponse(hashService.verify(request.password(), request.hash()));
    }
}
