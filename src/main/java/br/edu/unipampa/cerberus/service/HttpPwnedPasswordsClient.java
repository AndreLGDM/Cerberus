package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.config.BreachProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementação HTTP de {@link PwnedPasswordsClient} usando a API pública
 * Pwned Passwords (<a href="https://haveibeenpwned.com/API/v3#PwnedPasswords">docs</a>).
 *
 * <p>Envia o cabeçalho {@code Add-Padding: true}, que instrui a API a devolver
 * um número aleatório de registros falsos junto aos reais, dificultando a
 * inferência da senha consultada a partir do tamanho da resposta.</p>
 */
@Component
public class HttpPwnedPasswordsClient implements PwnedPasswordsClient {

    private final RestClient restClient;

    public HttpPwnedPasswordsClient(BreachProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeoutMs());
        factory.setReadTimeout(properties.getTimeoutMs());
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Add-Padding", "true")
                .defaultHeader("User-Agent", "Cerberus/1.0 (artefato academico UNIPAMPA)")
                .build();
    }

    @Override
    public String fetchRange(String prefix) {
        try {
            return restClient.get()
                    .uri("/range/{prefix}", prefix)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new BreachServiceException("Falha ao consultar Pwned Passwords", e);
        }
    }
}
