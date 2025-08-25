package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.DeepSeekTokenService;

public class DeepSeekTokenServiceExample {
    public static void main(String[] args) {
        System.out.println("Testing DeepSeekTokenService with HuggingFace tokenizer...");
        
        DeepSeekTokenService service = new DeepSeekTokenService();
        
        // Test verschiedene Texte
        String[] testTexts = {
            "Hello!",
            "Hello, world!",
            "This is a longer text to test tokenization.",
            "Dieser Text enthält deutsche Wörter.",
            "这是一个中文测试。",
            ""
        };
        
        for (int i = 0; i < testTexts.length; i++) {
            String text = testTexts[i];
            int tokenCount = service.calculateTokenCount(text);
            System.out.printf("Test %d: \"%s...\" -> %d tokens%n", 
                i + 1, 
                text.length() > 50 ? text.substring(0, 50) : text, 
                tokenCount
            );
        }
    }
}
