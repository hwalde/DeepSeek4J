package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.*;
import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.chat.completion.DeepSeekCompletionResponse;
import org.json.JSONObject;

/**
 * Demonstrates how to set "response_format":{"type":"json_object"} and ask
 * the model to strictly produce JSON. We'll parse the final JSON ourselves.
 */
public class DeepSeekChatCompletionWithStructuredOutputExample {

    public static void main(String[] args) {
        DeepSeekClient client = new DeepSeekClient();
        
        // We'll instruct the model to produce JSON with a question/answer
        String systemPrompt = """
        You are an assistant. You must output valid JSON.
        Please parse the "question" and "answer" from user input.
        Return them in a JSON object with fields "question" and "answer".
        """;

        // Build the request
        DeepSeekCompletionResponse response = client.chat().completion()
                .model("deepseek-chat")
                .addSystemMessage(systemPrompt)
                .addUserMessage("What's the capital of Germany? It's Berlin.")
                .responseFormat(DeepSeekResponseFormat.jsonObject())
                .maxTokens(512)
                .execute();

        // The model's final message should be JSON
        String assistantContent = response.assistantMessage();
        System.out.println("Assistant raw content: " + assistantContent);

        // Attempt to parse
        try {
            JSONObject obj = new JSONObject(assistantContent);
            String question = obj.optString("question", "");
            String answer = obj.optString("answer", "");
            System.out.println("Parsed question: " + question);
            System.out.println("Parsed answer: " + answer);
        } catch (Exception e) {
            System.err.println("Failed to parse model's output as JSON: " + e.getMessage());
        }

        System.out.println("finish_reason: " + response.finishReason());
    }
}
