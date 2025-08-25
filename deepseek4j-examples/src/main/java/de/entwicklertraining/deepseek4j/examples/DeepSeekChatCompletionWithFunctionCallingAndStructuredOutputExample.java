package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.*;
import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.chat.completion.DeepSeekCompletionResponse;
import org.json.JSONObject;

import java.util.List;

/**
 * Demonstrates both function calling and structured JSON output.
 * We'll define two tools: "fetchWeather" and "scheduleMeeting", then
 * produce a final JSON with fields "weather" and "meeting".
 */
public class DeepSeekChatCompletionWithFunctionCallingAndStructuredOutputExample {

    // Some naive record classes for the final JSON
    public record Weather(String city, String forecast) {}
    public record Meeting(String topic, String date, String time, List<String> participants) {}
    public record CombinedOutput(Weather weather, Meeting meeting) {}

    public static void main(String[] args) {
        DeepSeekClient client = new DeepSeekClient();
        
        // 1) Define fetchWeather function
        DeepSeekToolDefinition weatherTool = DeepSeekToolDefinition.builder("fetchWeather")
                .description("Fetch the weather for a city.")
                .parameter("location", DeepSeekJsonSchema.stringSchema("City name"), true)
                .callback(context -> {
                    String city = context.arguments().optString("location", "Unknown");
                    // Fake call
                    String json = "{\"city\":\"" + city + "\", \"forecast\":\"Sunny\"}";
                    return DeepSeekToolResult.of(json);
                })
                .build();

        // 2) Define scheduleMeeting function
        DeepSeekToolDefinition meetingTool = DeepSeekToolDefinition.builder("scheduleMeeting")
                .description("Schedule a meeting with topic, date, time, participants.")
                .parameter("topic", DeepSeekJsonSchema.stringSchema("Meeting topic"), true)
                .parameter("date", DeepSeekJsonSchema.stringSchema("YYYY-MM-DD"), true)
                .parameter("time", DeepSeekJsonSchema.stringSchema("HH:mm"), true)
                .parameter("participants",
                        DeepSeekJsonSchema.arraySchema(
                                DeepSeekJsonSchema.stringSchema("Participant name")
                        ),
                        true)
                .callback(context -> {
                    String topic = context.arguments().optString("topic", "No topic");
                    String date = context.arguments().optString("date", "2025-01-01");
                    String time = context.arguments().optString("time", "00:00");
                    // Participants is an array
                    List<Object> participants = context.arguments().optJSONArray("participants").toList();

                    // Fake scheduling
                    JSONObject res = new JSONObject();
                    res.put("topic", topic);
                    res.put("date", date);
                    res.put("time", time);
                    res.put("participants", participants);
                    return DeepSeekToolResult.of(res.toString());
                })
                .build();

        // 3) We'll ask the model: "Give me weather in Berlin, then schedule a meeting about 'Budget' tomorrow 10:00 with Alice & Bob."
        // Then we want the final output as a JSON with "weather" and "meeting" fields.
        String systemPrompt = """
        You can call the "fetchWeather" and "scheduleMeeting" functions if needed.
        Then produce a JSON object with fields "weather" and "meeting", each containing relevant data.
        """;

        DeepSeekCompletionResponse finalResponse = client.chat().completion()
                .model("deepseek-chat")
                .addSystemMessage(systemPrompt)
                .addUserMessage("What's the weather in Berlin today? Then schedule a meeting about 'Budget' on 2025-01-08 at 10:00 with Alice and Bob.")
                .responseFormat(DeepSeekResponseFormat.jsonObject()) // ensure JSON in final answer
                .addTool(weatherTool)
                .addTool(meetingTool)
                .execute();

        // Let's see the final content
        String content = finalResponse.assistantMessage();
        System.out.println("Assistant raw content: " + content);

        // Attempt to parse as JSON
        try {
            JSONObject obj = new JSONObject(content);
            JSONObject w = obj.getJSONObject("weather");
            JSONObject m = obj.getJSONObject("meeting");
            System.out.println("Weather => city=" + w.optString("city") + ", forecast=" + w.optString("forecast"));
            System.out.println("Meeting => topic=" + m.optString("topic") +
                    ", date=" + m.optString("date") +
                    ", time=" + m.optString("time") +
                    ", participants=" + m.optJSONArray("participants"));
        } catch (Exception e) {
            System.err.println("Failed to parse final JSON: " + e.getMessage());
        }

        System.out.println("finish_reason: " + finalResponse.finishReason());
    }
}
