package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.dto.HashResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashServiceTest {

    private final PasswordHashService service = new PasswordHashService();

    @Test
    void deveGerarHashBcryptVerificavel() {
        HashResponse response = service.hash("MinhaSenh@Segura1");

        assertThat(response.algorithm()).isEqualTo("bcrypt");
        assertThat(response.hash()).startsWith("$2");
        assertThat(service.verify("MinhaSenh@Segura1", response.hash())).isTrue();
    }

    @Test
    void naoDeveVerificarSenhaIncorreta() {
        HashResponse response = service.hash("MinhaSenh@Segura1");

        assertThat(service.verify("senhaErrada", response.hash())).isFalse();
    }

    @Test
    void deveGerarSaltDiferenteACadaHash() {
        String h1 = service.hash("mesmaSenha").hash();
        String h2 = service.hash("mesmaSenha").hash();

        // Salts aleatórios devem produzir hashes diferentes para a mesma senha.
        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    void deveRetornarFalseParaHashInvalido() {
        assertThat(service.verify("qualquer", "isto-nao-e-um-hash-bcrypt")).isFalse();
    }
}
