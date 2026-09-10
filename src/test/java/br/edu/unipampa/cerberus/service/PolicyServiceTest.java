package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.config.PolicyProperties;
import br.edu.unipampa.cerberus.dto.PolicyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyServiceTest {

    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        PasswordStrengthService strengthService = new PasswordStrengthService();
        strengthService.loadCommonPasswords();

        // Cliente falso: nenhuma senha é considerada vazada (corpo vazio), sem rede.
        BreachCheckService breachService = new BreachCheckService(prefix -> "");

        policyService = new PolicyService(new PolicyProperties(), strengthService, breachService);
    }

    @Test
    void deveAprovarSenhaConformeAPolitica() {
        PolicyResponse result = policyService.evaluate("Str0ng!Passphrase2024");

        assertThat(result.compliant()).isTrue();
        assertThat(result.violations()).isEmpty();
    }

    @Test
    void deveListarViolacoesDeSenhaFraca() {
        PolicyResponse result = policyService.evaluate("abc");

        assertThat(result.compliant()).isFalse();
        assertThat(result.violations())
                .anyMatch(v -> v.contains("caracteres"))       // comprimento mínimo
                .anyMatch(v -> v.contains("maiúscula"))
                .anyMatch(v -> v.contains("dígito"))
                .anyMatch(v -> v.contains("símbolo"));
    }

    @Test
    void deveReprovarSenhaComumMesmoQueLonga() {
        // Longa o suficiente, mas presente no dicionário de senhas comuns.
        PolicyResponse result = policyService.evaluate("qwertyuiop");

        assertThat(result.compliant()).isFalse();
        assertThat(result.violations())
                .anyMatch(v -> v.toLowerCase().contains("comum") || v.toLowerCase().contains("previsível"));
    }
}
