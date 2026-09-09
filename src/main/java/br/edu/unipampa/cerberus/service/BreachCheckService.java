package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.dto.BreachResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Verifica se uma senha aparece em vazamentos conhecidos, usando o modelo de
 * <em>k-anonymity</em> da API Pwned Passwords.
 *
 * <p><strong>Privacidade:</strong> a senha nunca sai do servidor. Calcula-se o
 * SHA-1 da senha e envia-se à API apenas os <em>5 primeiros</em> caracteres
 * hexadecimais do hash. A API devolve todos os sufixos que compartilham esse
 * prefixo, e a comparação final é feita localmente.</p>
 *
 * <p><strong>Nota sobre SHA-1:</strong> o SHA-1 é usado aqui exclusivamente
 * porque é o protocolo exigido pela API Pwned Passwords para o range de hashes;
 * ele <em>não</em> é usado para armazenar senhas (para isso, veja
 * {@link PasswordHashService}, que usa BCrypt).</p>
 */
@Service
public class BreachCheckService {

    private static final Logger log = LoggerFactory.getLogger(BreachCheckService.class);

    private final PwnedPasswordsClient client;

    public BreachCheckService(PwnedPasswordsClient client) {
        this.client = client;
    }

    /**
     * @return {@link BreachResponse} descrevendo o resultado; se a API estiver
     *         indisponível, retorna {@link BreachResponse#unavailable()} em vez
     *         de propagar a exceção — degradação graciosa.
     */
    public BreachResponse check(String password) {
        String sha1 = sha1Hex(password).toUpperCase();
        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);

        try {
            String body = client.fetchRange(prefix);
            long count = findCount(body, suffix);
            return count > 0 ? BreachResponse.breached(count) : BreachResponse.notBreached();
        } catch (BreachServiceException e) {
            log.warn("Verificação de vazamento indisponível: {}", e.getMessage());
            return BreachResponse.unavailable();
        }
    }

    /** Procura o sufixo do hash no corpo retornado e devolve sua contagem (0 se ausente). */
    long findCount(String body, String suffix) {
        if (body == null || body.isBlank()) {
            return 0;
        }
        for (String line : body.split("\\r?\\n")) {
            int sep = line.indexOf(':');
            if (sep < 0) {
                continue;
            }
            String lineSuffix = line.substring(0, sep).trim();
            if (lineSuffix.equalsIgnoreCase(suffix)) {
                try {
                    return Long.parseLong(line.substring(sep + 1).trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 é garantido pela plataforma Java; não deve ocorrer.
            throw new IllegalStateException("SHA-1 indisponível na JVM", e);
        }
    }
}
