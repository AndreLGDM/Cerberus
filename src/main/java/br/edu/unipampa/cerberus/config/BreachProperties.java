package br.edu.unipampa.cerberus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do cliente da API Pwned Passwords, carregada de
 * {@code cerberus.breach.*}.
 */
@ConfigurationProperties(prefix = "cerberus.breach")
public class BreachProperties {

    /** URL base da API Pwned Passwords (permite apontar para um mock em testes). */
    private String baseUrl = "https://api.pwnedpasswords.com";

    /** Tempo máximo, em milissegundos, de espera pela API externa. */
    private int timeoutMs = 4000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
