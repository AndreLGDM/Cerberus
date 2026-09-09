package br.edu.unipampa.cerberus.service;

/**
 * Abstração do acesso à API Pwned Passwords.
 *
 * <p>Isolar a chamada HTTP atrás de uma interface torna o
 * {@link BreachCheckService} testável sem rede: os testes fornecem uma
 * implementação falsa que devolve corpos de resposta controlados.</p>
 */
public interface PwnedPasswordsClient {

    /**
     * Consulta o intervalo (range) de um prefixo de hash SHA-1.
     *
     * @param prefix os 5 primeiros caracteres hexadecimais do SHA-1 da senha
     * @return corpo bruto da resposta, com linhas no formato {@code SUFIXO:CONTAGEM}
     * @throws BreachServiceException se a API estiver indisponível
     */
    String fetchRange(String prefix);
}
