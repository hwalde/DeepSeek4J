package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.chat.completion.DeepSeekCompletionResponse;
import de.entwicklertraining.deepseek4j.DeepSeekResponseFormat;

import java.util.List;

/**
 * An advanced example using the DeepSeek Chat Completion API with all available options
 * except tool calling and JSON mode.
 * 
 * This example demonstrates:
 * 1. Setting all request parameters
 * 2. Extracting all information from the response
 * 3. Displaying the raw JSON response
 */
public class DeepSeekChatCompletionAdvancedExample {
    public static void main(String[] args) {
        DeepSeekClient client = new DeepSeekClient();
        
        // Build an advanced request with all available options except tool calling and JSON mode
        DeepSeekCompletionResponse response = client.chat().completion()
                .model("deepseek-reasoner") // Required: Specify the model to use
                .addSystemMessage("You are a helpful assistant specialized in explaining Java programming concepts.", "System") // With name parameter
                .addUserMessage("Can you explain the difference between ArrayList and LinkedList in Java?", "User") // With name parameter
                .addAssistantMessage("I'd be happy to explain the differences between these two List implementations.", "Assistant") // With name parameter
                .addUserMessage("Please include performance characteristics and use cases.", "User") // With name parameter
                .frequencyPenalty(0.5) // Controls repetition: higher values reduce repetition
                .presencePenalty(0.5) // Controls topic diversity: higher values encourage new topics
                .maxTokens(2048) // Limits response length
                .responseFormat(DeepSeekResponseFormat.text()) // Use text format (not JSON)
                .stop(List.of("END", "STOP")) // Custom stop sequences
                //.stream(false) // Disable streaming for this example
                //.streamOptions(DeepSeekCompletionRequest.DeepSeekStreamOptions.withUsage(true)) // Include usage in stream
                .temperature(0.7) // Controls randomness: higher values make output more random
                .topP(0.9) // Controls diversity via nucleus sampling
                //.logprobs(true) // Enable log probabilities
                //.topLogprobs(3) // Return top 3 most likely tokens
                .execute();

        // First, output the raw JSON response
        System.out.println("=== RAW JSON RESPONSE ===");
        System.out.println(response.getJson().toString(2));
        System.out.println("\n");

        // Output all information from the response
        System.out.println("=== RESPONSE DETAILS ===");
        
        // Top-level information
        System.out.println("ID: " + response.getId());
        System.out.println("Object: " + response.getObject());
        System.out.println("Created: " + response.getCreated());
        System.out.println("Model: " + response.getModel());
        System.out.println("System Fingerprint: " + response.getSystemFingerprint());
        
        // Usage information
        DeepSeekCompletionResponse.Usage usage = response.getUsage();
        if (usage != null) {
            System.out.println("\n=== USAGE STATISTICS ===");
            System.out.println("Prompt Tokens: " + usage.getPromptTokens());
            System.out.println("Completion Tokens: " + usage.getCompletionTokens());
            System.out.println("Total Tokens: " + usage.getTotalTokens());
            System.out.println("Prompt Cache Hit Tokens: " + usage.getPromptCacheHitTokens());
            System.out.println("Prompt Cache Miss Tokens: " + usage.getPromptCacheMissTokens());
            
            // Completion tokens details (if available)
            DeepSeekCompletionResponse.CompletionTokensDetails details = usage.getCompletionTokensDetails();
            if (details != null) {
                System.out.println("Reasoning Tokens: " + details.getReasoningTokens());
            }
        }
        
        // Choices information
        System.out.println("\n=== CHOICES ===");
        List<DeepSeekCompletionResponse.Choice> choices = response.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            DeepSeekCompletionResponse.Choice choice = choices.get(i);
            System.out.println("Choice #" + (i + 1));
            System.out.println("  Index: " + choice.getIndex());
            System.out.println("  Finish Reason: " + choice.getFinishReason());
            
            // Message information
            DeepSeekCompletionResponse.Message message = choice.getMessage();
            if (message != null) {
                System.out.println("  Message Role: " + message.getRole());
                System.out.println("  Message Content: " + message.getContent());
                
                // Reasoning content (if available)
                String reasoningContent = message.getReasoningContent();
                if (reasoningContent != null && !reasoningContent.isEmpty()) {
                    System.out.println("  Reasoning Content: " + reasoningContent);
                }
            }
            
            // Log probabilities (if enabled)
            DeepSeekCompletionResponse.Logprobs logprobs = choice.getLogprobs();
            if (logprobs != null) {
                System.out.println("\n  === LOG PROBABILITIES ===");
                List<DeepSeekCompletionResponse.TokenLogprob> tokenLogprobs = logprobs.getContent();
                for (int j = 0; j < Math.min(5, tokenLogprobs.size()); j++) { // Show first 5 tokens only
                    DeepSeekCompletionResponse.TokenLogprob tokenLogprob = tokenLogprobs.get(j);
                    System.out.println("    Token: " + tokenLogprob.getToken());
                    System.out.println("    Logprob: " + tokenLogprob.getLogprob());
                    
                    // Top alternative tokens
                    List<DeepSeekCompletionResponse.TopTokenLogprob> topLogprobs = tokenLogprob.getTopLogprobs();
                    if (!topLogprobs.isEmpty()) {
                        System.out.println("    Top Alternatives:");
                        for (DeepSeekCompletionResponse.TopTokenLogprob topLogprob : topLogprobs) {
                            System.out.println("      Token: " + topLogprob.getToken() + ", Logprob: " + topLogprob.getLogprob());
                        }
                    }
                }
                if (tokenLogprobs.size() > 5) {
                    System.out.println("    ... (showing only first 5 tokens)");
                }
            }
        }
        
        // Convenience methods
        System.out.println("\n=== CONVENIENCE METHODS ===");
        System.out.println("Assistant Message: " + response.assistantMessage());
        System.out.println("Finish Reason: " + response.finishReason());
    }
}