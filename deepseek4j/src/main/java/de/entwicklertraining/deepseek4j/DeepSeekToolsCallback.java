package de.entwicklertraining.deepseek4j;

@FunctionalInterface
public interface DeepSeekToolsCallback {
    /**
     * Handle the tool call. The tool's arguments are provided in context.arguments().
     * Return some string result, typically as JSON.
     */
    DeepSeekToolResult handle(DeepSeekToolCallContext context);
}
