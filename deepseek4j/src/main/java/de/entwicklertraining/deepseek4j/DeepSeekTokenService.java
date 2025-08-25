package de.entwicklertraining.deepseek4j;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.huggingface.tokenizers.Encoding;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Tokenzähler für DeepSeek-Modelle (Byte-Level BPE).
 */
public final class DeepSeekTokenService {

    private static final double AVG_LATIN_CHARS_PER_TOKEN = 3.3; // 1 / 0.3  ➜ konservativ
    private static final double AVG_CJK_CHARS_PER_TOKEN   = 1.7; // 1 / 0.6
    private final HuggingFaceTokenizer tokenizer;

    public DeepSeekTokenService() {
        tokenizer = loadTokenizer();
    }

    private HuggingFaceTokenizer loadTokenizer() {
        try {
            // Lädt lokalen Tokenizer aus resources
            String resourcePath = getClass().getClassLoader().getResource("tokenizer.json").getPath();
            Path tokenizerPath = Paths.get(resourcePath).getParent();
            
            Map<String, String> options = new HashMap<>();
            options.put("trust_remote_code", "true");
            
            return HuggingFaceTokenizer.newInstance(tokenizerPath, options);
        } catch (Exception e) {
            // Fallback: versuche relativen Pfad für Entwicklung
            try {
                Path tokenizerPath = Paths.get("src/main/resources/");
                Map<String, String> options = new HashMap<>();
                options.put("trust_remote_code", "true");
                return HuggingFaceTokenizer.newInstance(tokenizerPath, options);
            } catch (IOException fallbackEx) {
                return null; // Fallback aktiviert
            }
        }
    }

    public int calculateTokenCount(String text) {
        if (text == null || text.isEmpty()) return 0;

        try {
            if (tokenizer != null) {
                return tokenizer.encode(text).getIds().length;
            }
        } catch (Exception ignored) {
            // gehe zur Heuristik
        }

        long latin = text.codePoints()
                .filter(cp -> Character.UnicodeScript.of(cp)
                        .equals(Character.UnicodeScript.LATIN))
                .count();

        long cjk   = text.length() - latin;

        double estimate = latin  / AVG_LATIN_CHARS_PER_TOKEN
                + cjk    / AVG_CJK_CHARS_PER_TOKEN;

        return (int) Math.ceil(estimate);
    }
}
