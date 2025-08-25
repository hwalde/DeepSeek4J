package de.entwicklertraining.deepseek4j;

public record DeepSeekToolResult(String content) {
    public static DeepSeekToolResult of(String content) {
        return new DeepSeekToolResult(content);
    }
}
