package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.chat.completion.DeepSeekCompletionResponse;
import de.entwicklertraining.deepseek4j.DeepSeekResponseFormat;

/**
 * A simple example using the DeepSeek Chat Completion API without function calling or structured output.
 */
public class DeepSeekChatCompletionExample {
    public static void main(String[] args) {
        DeepSeekClient client = new DeepSeekClient();
        
        // Build a simple request
        DeepSeekCompletionResponse response = client.chat().completion()
                .model("deepseek-chat")
                .addSystemMessage("You are a helpful assistant.")
                .addUserMessage("Hi, how are you?")
                .responseFormat(DeepSeekResponseFormat.text()) // or .jsonObject()
                .maxTokens(1024)
                .execute();

        // Print the final assistant message
        System.out.println("Assistant says: " + response.assistantMessage());
        System.out.println("finish_reason: " + response.finishReason());

        // Let's also print the raw JSON for debugging
        System.out.println("Raw JSON:");
        System.out.println(response.getJson().toString(2));
    }
}
