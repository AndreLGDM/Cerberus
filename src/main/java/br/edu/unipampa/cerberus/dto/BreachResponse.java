package br.edu.unipampa.cerberus.dto;

/**
 * Resultado da verificação de vazamento contra a base Pwned Passwords.
 *
 * @param checked  indica se a verificação pôde ser realizada (false = API indisponível)
 * @param breached true se a senha aparece em vazamentos conhecidos
 * @param count    número de vezes que a senha apareceu em vazamentos (0 se não encontrada)
 * @param message  mensagem legível resumindo o resultado
 */
public record BreachResponse(
        boolean checked,
        boolean breached,
        long count,
        String message) {

    public static BreachResponse notBreached() {
        return new BreachResponse(true, false, 0,
                "Senha não encontrada em vazamentos conhecidos.");
    }

    public static BreachResponse breached(long count) {
        return new BreachResponse(true, true, count,
                "Senha encontrada em " + count + " vazamento(s) conhecido(s). Não a utilize.");
    }

    public static BreachResponse unavailable() {
        return new BreachResponse(false, false, 0,
                "Não foi possível consultar a base de vazamentos no momento.");
    }
}
