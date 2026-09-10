package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.dto.BreachResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BreachCheckServiceTest {

    @Test
    void deveEnviarApenasPrefixoDe5CaracteresParaAApi() {
        // Garante o modelo de k-anonymity: só o prefixo do hash sai do servidor.
        String[] capturado = new String[1];
        PwnedPasswordsClient client = prefix -> {
            capturado[0] = prefix;
            return "";
        };

        new BreachCheckService(client).check("qualquer-senha");

        assertThat(capturado[0]).hasSize(5);
        assertThat(capturado[0]).matches("[0-9A-F]{5}");
    }

    @Test
    void deveDetectarSenhaVazada() {
        String sha1 = BreachCheckService.sha1Hex("password").toUpperCase();
        String suffix = sha1.substring(5);
        // A API devolve linhas "SUFIXO:CONTAGEM"; incluímos o sufixo real com contagem alta.
        PwnedPasswordsClient client = prefix -> "0123456789012345678901234567890ABCD:3\r\n" + suffix + ":99999";

        BreachResponse result = new BreachCheckService(client).check("password");

        assertThat(result.checked()).isTrue();
        assertThat(result.breached()).isTrue();
        assertThat(result.count()).isEqualTo(99999);
    }

    @Test
    void deveTratarSenhaNaoVazada() {
        PwnedPasswordsClient client = prefix -> "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA:1\r\nBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB:2";

        BreachResponse result = new BreachCheckService(client).check("uma-senha-improvavel-de-vazar-xyz");

        assertThat(result.checked()).isTrue();
        assertThat(result.breached()).isFalse();
        assertThat(result.count()).isZero();
    }

    @Test
    void deveDegradarGraciosamenteQuandoApiIndisponivel() {
        PwnedPasswordsClient client = prefix -> {
            throw new BreachServiceException("rede indisponível", new RuntimeException());
        };

        BreachResponse result = new BreachCheckService(client).check("password");

        assertThat(result.checked()).isFalse();
        assertThat(result.breached()).isFalse();
        assertThat(result.message()).containsIgnoringCase("não foi possível");
    }

    @Test
    void deveCalcularSha1CorretamenteParaVetorConhecido() {
        // Vetor de teste conhecido: SHA-1("password").
        assertThat(BreachCheckService.sha1Hex("password").toUpperCase())
                .isEqualTo("5BAA61E4C9B93F3F0682250B6CF8331B7EE68FD8");
    }
}
