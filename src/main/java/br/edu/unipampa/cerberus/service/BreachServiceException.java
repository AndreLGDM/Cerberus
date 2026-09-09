package br.edu.unipampa.cerberus.service;

/**
 * Lançada quando a base de vazamentos não pôde ser consultada
 * (ex.: rede indisponível, timeout ou erro da API externa).
 */
public class BreachServiceException extends RuntimeException {

    public BreachServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
