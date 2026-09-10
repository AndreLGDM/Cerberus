package br.edu.unipampa.cerberus.controller;

import br.edu.unipampa.cerberus.dto.BreachResponse;
import br.edu.unipampa.cerberus.dto.PasswordRequest;
import br.edu.unipampa.cerberus.dto.PolicyResponse;
import br.edu.unipampa.cerberus.dto.StrengthResponse;
import br.edu.unipampa.cerberus.service.BreachCheckService;
import br.edu.unipampa.cerberus.service.PasswordStrengthService;
import br.edu.unipampa.cerberus.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de auditoria de senhas: força, vazamento e política.
 */
@RestController
@RequestMapping("/api/v1/password")
@Tag(name = "Password", description = "Análise de força, verificação de vazamento e avaliação de política")
public class PasswordController {

    private final PasswordStrengthService strengthService;
    private final BreachCheckService breachCheckService;
    private final PolicyService policyService;

    public PasswordController(PasswordStrengthService strengthService,
                              BreachCheckService breachCheckService,
                              PolicyService policyService) {
        this.strengthService = strengthService;
        this.breachCheckService = breachCheckService;
        this.policyService = policyService;
    }

    @Operation(summary = "Analisa a força de uma senha (entropia + heurísticas)")
    @PostMapping("/strength")
    public StrengthResponse strength(@Valid @RequestBody PasswordRequest request) {
        return strengthService.analyze(request.password());
    }

    @Operation(summary = "Verifica se a senha aparece em vazamentos (HaveIBeenPwned, k-anonymity)")
    @PostMapping("/breach")
    public BreachResponse breach(@Valid @RequestBody PasswordRequest request) {
        return breachCheckService.check(request.password());
    }

    @Operation(summary = "Auditoria completa: avalia a senha contra a política configurada")
    @PostMapping("/policy")
    public PolicyResponse policy(@Valid @RequestBody PasswordRequest request) {
        return policyService.evaluate(request.password());
    }
}
