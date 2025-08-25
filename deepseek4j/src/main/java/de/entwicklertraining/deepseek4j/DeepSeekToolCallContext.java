package de.entwicklertraining.deepseek4j;

import org.json.JSONObject;

/**
 * Encapsulates the arguments that the model provided when calling a tool.
 */
public record DeepSeekToolCallContext(JSONObject arguments) {}
