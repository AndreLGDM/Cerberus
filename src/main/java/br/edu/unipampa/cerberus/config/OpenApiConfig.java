package br.edu.unipampa.cerberus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentação OpenAPI expostos em {@code /docs} (Swagger UI)
 * e {@code /v3/api-docs} (JSON).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI passGuardOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Cerberus API")
                .description("Serviço de auditoria de segurança de senhas: análise de força, "
                        + "verificação de vazamento (HaveIBeenPwned via k-anonymity) e hashing seguro (BCrypt).")
                .version("1.0.0")
                .contact(new Contact().name("André - UNIPAMPA"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
