package br.edu.unipampa.cerberus.service;

import br.edu.unipampa.cerberus.dto.StrengthResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Avalia a força de uma senha combinando uma estimativa de entropia com um
 * conjunto de heurísticas (senhas comuns, sequências de teclado e repetições).
 *
 * <p>A entropia é estimada como {@code comprimento * log2(tamanho_do_alfabeto)},
 * onde o alfabeto cresce conforme as classes de caracteres presentes. É uma
 * estimativa de <em>limite superior</em> — por isso as heurísticas aplicam
 * penalidades para padrões previsíveis que a fórmula sozinha superestimaria.</p>
 */
@Service
public class PasswordStrengthService {

    private static final Logger log = LoggerFactory.getLogger(PasswordStrengthService.class);

    /** Rótulos por score (índice = score de 0 a 4). */
    private static final String[] RATINGS = {
            "MUITO_FRACA", "FRACA", "MODERADA", "FORTE", "MUITO_FORTE"
    };

    /** 4 ou mais caracteres iguais em sequência. */
    private static final Pattern REPEATED = Pattern.compile("(.)\\1{3,}");

    private static final String[] KEYBOARD_ROWS = {
            "abcdefghijklmnopqrstuvwxyz",
            "0123456789",
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm"
    };

    private final Set<String> commonPasswords = new HashSet<>();

    @PostConstruct
    void loadCommonPasswords() {
        ClassPathResource resource = new ClassPathResource("common-passwords.txt");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim().toLowerCase();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    commonPasswords.add(trimmed);
                }
            }
            log.info("Dicionário de senhas comuns carregado: {} entradas", commonPasswords.size());
        } catch (IOException e) {
            // Sem o dicionário o serviço ainda funciona; apenas perde uma heurística.
            log.warn("Não foi possível carregar o dicionário de senhas comuns: {}", e.getMessage());
        }
    }

    public StrengthResponse analyze(String password) {
        int length = password.length();

        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        int poolSize = 0;
        if (hasLower) poolSize += 26;
        if (hasUpper) poolSize += 26;
        if (hasDigit) poolSize += 10;
        if (hasSymbol) poolSize += 33;

        double entropyBits = poolSize > 0
                ? length * (Math.log(poolSize) / Math.log(2))
                : 0.0;
        entropyBits = Math.round(entropyBits * 100.0) / 100.0;

        boolean isCommon = isCommonPassword(password);
        boolean hasRepeated = REPEATED.matcher(password).find();
        boolean hasSequential = hasSequentialRun(password.toLowerCase());

        int score = scoreFromEntropy(entropyBits);

        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (isCommon) {
            score = 0;
            warnings.add("Senha presente em listas de senhas mais comuns.");
        }
        if (hasRepeated) {
            score = Math.max(0, score - 1);
            warnings.add("Contém caracteres repetidos em sequência (ex.: 'aaaa').");
        }
        if (hasSequential) {
            score = Math.max(0, score - 1);
            warnings.add("Contém uma sequência previsível de teclado/alfabeto (ex.: 'abcd', '1234', 'qwerty').");
        }
        if (length < 8) {
            score = Math.min(score, 1);
            warnings.add("Senha muito curta (menos de 8 caracteres).");
        }

        buildSuggestions(length, hasLower, hasUpper, hasDigit, hasSymbol, suggestions);

        return new StrengthResponse(
                score, RATINGS[score], entropyBits, length,
                hasLower, hasUpper, hasDigit, hasSymbol, warnings, suggestions);
    }

    private int scoreFromEntropy(double entropyBits) {
        if (entropyBits < 28) return 0;
        if (entropyBits < 36) return 1;
        if (entropyBits < 60) return 2;
        if (entropyBits < 128) return 3;
        return 4;
    }

    private void buildSuggestions(int length, boolean hasLower, boolean hasUpper,
                                  boolean hasDigit, boolean hasSymbol, List<String> suggestions) {
        if (length < 12) {
            suggestions.add("Use pelo menos 12 caracteres (idealmente 16 ou mais).");
        }
        if (!hasUpper) suggestions.add("Adicione letras maiúsculas.");
        if (!hasLower) suggestions.add("Adicione letras minúsculas.");
        if (!hasDigit) suggestions.add("Adicione dígitos.");
        if (!hasSymbol) suggestions.add("Adicione símbolos (ex.: !@#$%).");
        if (suggestions.isEmpty()) {
            suggestions.add("Considere usar uma frase-senha longa e única para cada serviço.");
        }
    }

    boolean isCommonPassword(String password) {
        String lower = password.toLowerCase();
        if (commonPasswords.contains(lower)) {
            return true;
        }
        // Remove dígitos/símbolos ao final (ex.: "senha123!" -> "senha") e reavalia.
        String core = lower.replaceAll("[^a-z]+$", "");
        return core.length() >= 3 && commonPasswords.contains(core);
    }

    /** Detecta 4+ caracteres consecutivos em qualquer linha de teclado, para frente ou para trás. */
    boolean hasSequentialRun(String lower) {
        for (String row : KEYBOARD_ROWS) {
            String reversed = new StringBuilder(row).reverse().toString();
            for (int i = 0; i + 4 <= lower.length(); i++) {
                String window = lower.substring(i, i + 4);
                if (row.contains(window) || reversed.contains(window)) {
                    return true;
                }
            }
        }
        return false;
    }
}
