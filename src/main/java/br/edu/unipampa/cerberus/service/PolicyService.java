package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.config.PolicyProperties;
import br.edu.unipampa.cerberus.dto.BreachResponse;
import br.edu.unipampa.cerberus.dto.PolicyResponse;
import br.edu.unipampa.cerberus.dto.StrengthResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Avalia uma senha contra a política configurada, reunindo os resultados de
 * força e de vazamento em um único veredito de conformidade.
 *
 * <p>Este é o endpoint de "auditoria completa": combina as três verificações do
 * artefato em uma resposta acionável (compliant / lista de violações).</p>
 */
@Service
public class PolicyService {

    private final PolicyProperties policy;
    private final PasswordStrengthService strengthService;
    private final BreachCheckService breachCheckService;

    public PolicyService(PolicyProperties policy,
                         PasswordStrengthService strengthService,
                         BreachCheckService breachCheckService) {
        this.policy = policy;
        this.strengthService = strengthService;
        this.breachCheckService = breachCheckService;
    }

    public PolicyResponse evaluate(String password) {
        StrengthResponse strength = strengthService.analyze(password);
        BreachResponse breach = policy.isForbidBreached()
                ? breachCheckService.check(password)
                : new BreachResponse(false, false, 0, "Verificação de vazamento desativada pela política.");

        List<String> violations = new ArrayList<>();

        if (password.length() < policy.getMinLength()) {
            violations.add("Deve ter ao menos " + policy.getMinLength() + " caracteres.");
        }
        if (policy.isRequireUppercase() && !strength.hasUppercase()) {
            violations.add("Deve conter ao menos uma letra maiúscula.");
        }
        if (policy.isRequireLowercase() && !strength.hasLowercase()) {
            violations.add("Deve conter ao menos uma letra minúscula.");
        }
        if (policy.isRequireDigit() && !strength.hasDigit()) {
            violations.add("Deve conter ao menos um dígito.");
        }
        if (policy.isRequireSymbol() && !strength.hasSymbol()) {
            violations.add("Deve conter ao menos um símbolo.");
        }
        if (policy.isForbidCommon() && strengthService.isCommonPassword(password)) {
            violations.add("Não pode ser uma senha comum/previsível.");
        }
        // Só reprova por vazamento quando a verificação foi de fato realizada.
        if (policy.isForbidBreached() && breach.checked() && breach.breached()) {
            violations.add("Não pode ser uma senha presente em vazamentos conhecidos.");
        }

        return new PolicyResponse(violations.isEmpty(), violations, strength, breach);
    }
}
