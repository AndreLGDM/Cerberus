package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.dto.StrengthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordStrengthServiceTest {

    private PasswordStrengthService service;

    @BeforeEach
    void setUp() {
        service = new PasswordStrengthService();
        // Em um teste unitário puro o @PostConstruct não é chamado pelo Spring,
        // então carregamos o dicionário manualmente.
        service.loadCommonPasswords();
    }

    @Test
    void deveClassificarSenhaComumComoMuitoFraca() {
        StrengthResponse result = service.analyze("password");

        assertThat(result.score()).isZero();
        assertThat(result.rating()).isEqualTo("MUITO_FRACA");
        assertThat(result.warnings())
                .anyMatch(w -> w.toLowerCase().contains("comuns"));
    }

    @Test
    void deveDetectarSenhaComumComSufixoNumerico() {
        // "senha" está no dicionário; "senha123" deve ser detectada pela remoção do sufixo.
        assertThat(service.isCommonPassword("senha123")).isTrue();
    }

    @Test
    void deveDetectarSequenciaDeTeclado() {
        assertThat(service.hasSequentialRun("qwerty")).isTrue();
        assertThat(service.hasSequentialRun("abcd")).isTrue();
        assertThat(service.hasSequentialRun("1234")).isTrue();
        assertThat(service.hasSequentialRun("x9k2")).isFalse();
    }

    @Test
    void devePontuarSenhaForteComAltoScore() {
        StrengthResponse result = service.analyze("9xQ!vTm2#Lp8zR");

        assertThat(result.score()).isGreaterThanOrEqualTo(3);
        assertThat(result.hasLowercase()).isTrue();
        assertThat(result.hasUppercase()).isTrue();
        assertThat(result.hasDigit()).isTrue();
        assertThat(result.hasSymbol()).isTrue();
        assertThat(result.entropyBits()).isGreaterThan(60.0);
    }

    @Test
    void devePenalizarCaracteresRepetidos() {
        StrengthResponse result = service.analyze("aaaaaaaa");

        assertThat(result.warnings())
                .anyMatch(w -> w.toLowerCase().contains("repetidos"));
        assertThat(result.score()).isLessThanOrEqualTo(1);
    }

    @Test
    void deveReportarClassesDeCaracteresCorretamente() {
        StrengthResponse result = service.analyze("abc");

        assertThat(result.hasLowercase()).isTrue();
        assertThat(result.hasUppercase()).isFalse();
        assertThat(result.hasDigit()).isFalse();
        assertThat(result.hasSymbol()).isFalse();
        assertThat(result.length()).isEqualTo(3);
        assertThat(result.suggestions()).isNotEmpty();
    }
}
