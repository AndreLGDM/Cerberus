package br.edu.unipampa.cerberus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Regras da política de senhas, carregadas de {@code cerberus.policy.*}.
 *
 * <p>Manter a política como configuração (e não fixa no código) melhora a
 * sustentabilidade do artefato: uma organização pode endurecer ou afrouxar as
 * regras por variáveis de ambiente, sem recompilar.</p>
 */
@ConfigurationProperties(prefix = "cerberus.policy")
public class PolicyProperties {

    private int minLength = 12;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSymbol = true;
    private boolean forbidCommon = true;
    private boolean forbidBreached = true;

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public void setRequireLowercase(boolean requireLowercase) {
        this.requireLowercase = requireLowercase;
    }

    public boolean isRequireDigit() {
        return requireDigit;
    }

    public void setRequireDigit(boolean requireDigit) {
        this.requireDigit = requireDigit;
    }

    public boolean isRequireSymbol() {
        return requireSymbol;
    }

    public void setRequireSymbol(boolean requireSymbol) {
        this.requireSymbol = requireSymbol;
    }

    public boolean isForbidCommon() {
        return forbidCommon;
    }

    public void setForbidCommon(boolean forbidCommon) {
        this.forbidCommon = forbidCommon;
    }

    public boolean isForbidBreached() {
        return forbidBreached;
    }

    public void setForbidBreached(boolean forbidBreached) {
        this.forbidBreached = forbidBreached;
    }
}
