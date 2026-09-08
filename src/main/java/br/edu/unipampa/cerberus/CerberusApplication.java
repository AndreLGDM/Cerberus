package br.edu.unipampa.cerberus;

import br.edu.unipampa.cerberus.config.BreachProperties;
import br.edu.unipampa.cerberus.config.PolicyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Ponto de entrada do serviço Cerberus.
 *
 * <p>Cerberus é um serviço REST de auditoria de segurança de senhas. Ele reúne, em um
 * único artefato, três verificações complementares de higiene de senhas:
 * análise de força (entropia + heurísticas), verificação de vazamento
 * (HaveIBeenPwned via k-anonymity) e hashing seguro (BCrypt).</p>
 */
@SpringBootApplication
@EnableConfigurationProperties({PolicyProperties.class, BreachProperties.class})
public class CerberusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CerberusApplication.class, args);
    }
}
