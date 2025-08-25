package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.*;
import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.chat.completion.DeepSeekCompletionResponse;

public class DeepSeekChatCompletionWithFunctionCallingExample {

    public static void main(String[] args) {
        DeepSeekClient client = new DeepSeekClient();
        
        // Define a tool that returns a simple "weather" info
        DeepSeekToolDefinition weatherTool = DeepSeekToolDefinition.builder("get_weather")
                .description("Get weather for a location.")
                .parameter("location", DeepSeekJsonSchema.stringSchema("The city"), true)
                .callback(context -> {
                    String loc = context.arguments().optString("location", "Unknown");
                    // Just a dummy response
                    String result = "{\"forecast\":\"Sunny\",\"city\":\"" + loc + "\"}";
                    return DeepSeekToolResult.of(result);
                })
                .build();

        // Build a request with the tool
        DeepSeekCompletionResponse response = client.chat().completion()
                .model("deepseek-chat")
                .addSystemMessage("You can use the get_weather function to fetch the weather.")
                .addUserMessage("What's the weather in Berlin?")
                .addTool(weatherTool)
                .execute();

        // Print final assistant content
        System.out.println("Assistant says: " + response.assistantMessage());
        System.out.println("finish_reason: " + response.finishReason());

        // Raw JSON debug
        System.out.println("Raw JSON:");
        System.out.println(response.getJson().toString(2));
    }
}
