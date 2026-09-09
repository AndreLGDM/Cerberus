package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.dto.HashResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Demonstra o armazenamento correto de senhas usando BCrypt.
 *
 * <p>BCrypt aplica um <em>salt</em> aleatório por senha (embutido no próprio
 * hash) e um custo (work factor) configurável, tornando ataques de força bruta
 * e o uso de rainbow tables inviáveis. O custo 12 equilibra segurança e
 * desempenho para o hardware atual.</p>
 */
@Service
public class PasswordHashService {

    private static final int BCRYPT_STRENGTH = 12;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    public HashResponse hash(String password) {
        return new HashResponse("bcrypt", encoder.encode(password));
    }

    /**
     * Compara em tempo constante uma senha com um hash BCrypt existente.
     *
     * @return true se a senha corresponde; false se não corresponde ou se o
     *         hash tiver formato inválido.
     */
    public boolean verify(String password, String hash) {
        try {
            return encoder.matches(password, hash);
        } catch (IllegalArgumentException e) {
            // Hash em formato inválido -> não corresponde, sem quebrar a API.
            return false;
        }
    }
}
