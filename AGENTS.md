Wichtig: Aktualisiere AGENTS.md nach jedem Task.
Wichtig: Aktualisiere die README.md nach jedem Task nur wenn die Informationen darin veraltet sind
# DeepSeek4J

DeepSeek4J is a fluent Java wrapper for the [DeepSeek API](https://platform.openai.com/docs/api-reference).
It builds on top of the lightweight [`api-base`](https://github.com/hwalde/api-base) library which
handles HTTP communication, authentication and exponential backoff. The goal is to provide a type safe
and convenient way to access DeepSeek services from modern Java (JDK&nbsp;21+).

## Features

* Chat Completions including tool calling, structured outputs and vision inputs
* Models API for listing available models
* User Balance API for checking account balance
* Token counting utilities 
* Fluent builder APIs for all requests
* Examples demonstrating each feature

## Installation

Add the dependency from Maven Central:

```xml
<dependency>
    <groupId>de.entwicklertraining</groupId>
    <artifactId>openai4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Usage

Instantiate a `DeepSeekClient` and use the builders exposed by its fluent API. The
[function calling example](examples/src/main/java/de/entwicklertraining/openai4j/examples/DeepSeekChatCompletionWithFunctionCallingExample.java)
shows how tools can be defined and executed:

```java
DeepSeekToolDefinition weatherFunction = DeepSeekToolDefinition.builder("get_local_weather")
        .description("Get weather information for a location.")
        .parameter("location", DeepSeekJsonSchema.stringSchema("Name of the city"), true)
        .callback(ctx -> {
            String loc = ctx.arguments().getString("location");
            return DeepSeekToolResult.of("Sunny in " + loc + " with a high of 25°C.");
        })
        .build();

DeepSeekClient client = new DeepSeekClient(); // this will read the API key from the environment variable OPENAI_API_KEY

DeepSeekChatCompletionResponse resp = client.chat().completion()
        .model("gpt-4o-mini")
        .addSystemMessage("You are a helpful assistant.")
        .addUserMessage("What's the weather in Berlin and the current time?")
        .addTool(weatherFunction)
        .execute();
System.out.println(resp.assistantMessage());
```
【F:examples/src/main/java/de/entwicklertraining/openai4j/examples/DeepSeekChatCompletionWithFunctionCallingExample.java†L10-L44】

The [DALL·E&nbsp;3 example](examples/src/main/java/de/entwicklertraining/openai4j/examples/DallE3Example.java)
illustrates image generation:

```java
DeepSeekClient client = new DeepSeekClient();
DallE3Response response = client.images().generations().dalle3()
        .prompt("A futuristic city floating in the sky, with neon lights")
        .size(ImageSize.SIZE_1024x1024)
        .responseFormat(ResponseFormat.B64_JSON)
        .n(1)
        .quality(ImageQuality.HD)
        .style(ImageStyle.VIVID)
        .execute();
List<String> images = response.images();
```
【F:examples/src/main/java/de/entwicklertraining/openai4j/examples/DallE3Example.java†L26-L43】

See the `examples` module for more demonstrations (embeddings, speech, translation, web search
and vision).

## Project Structure

The library follows a clear structure:

* **`DeepSeekClient`** – entry point for all API calls. Extends `ApiClient` from *api-base*
  and registers error handling. It exposes sub clients (`chat()`, `models()`, `user()`).
* **Request/Response classes** – located in packages like
  `chat.completion`, `models`, `user.balance`.
  Each request extends `DeepSeekRequest` and has an inner `Builder` that extends
  `ApiRequestBuilderBase` from *api-base*. Responses extend `DeepSeekResponse`.
* **Tool calling** – defined via `DeepSeekToolDefinition` and handled by
  `DeepSeekToolsCallback` and `DeepSeekToolCallContext`.
* **Structured outputs** – use `DeepSeekJsonSchema` and `DeepSeekResponseFormat`.
* **Token utilities** – `DeepSeekTokenService` counts tokens.

The examples directory mirrors these packages and can be used as a quick start.

## Extending DeepSeek4J

1. **Create a Request** – subclass `DeepSeekRequest` and implement `getRelativeUrl`,
   `getHttpMethod`, `getBody` and `createResponse`. Provide a nested builder
   extending `ApiRequestBuilderBase`.
2. **Create a Response** – subclass `DeepSeekResponse` and parse the JSON or binary
   payload returned by DeepSeek.
3. **Expose a builder** – add a convenience method in `DeepSeekClient` returning your
   new builder so users can call it fluently.

Thanks to *api-base*, sending the request is handled by calling
`client.sendRequest(request)` or by using the builder’s `execute()` method which
internally delegates to `sendRequest` with optional exponential backoff.
See [api-base’s Readme](https://github.com/hwalde/api-base) for details on available
settings like retries, timeouts or capture hooks.

## Building

This project uses Maven. Compile the library and run examples with:

```bash
mvn package
```

## License

DeepSeek4J is distributed under the MIT License as defined in the project `pom.xml`.


## Additional Details

This Maven-based project targets JDK 21 and provides a fluent Java wrapper around the DeepSeek REST API. The main module `deepseek4j` exposes builders for chat completions, models listing, and user balance checking. Example usages are located in `deepseek4j-examples/src/main/java/de/entwicklertraining/deepseek4j/examples`.

Important packages include:
- `chat.completion` – classes like `DeepSeekChatCompletionRequest` and `DeepSeekChatCompletionResponse` implement the Chat Completions API.
- `models` – includes `DeepSeekModelsRequest` and `DeepSeekModelsResponse` for listing available models.
- `user.balance` – includes `DeepSeekUserBalanceRequest` and `DeepSeekUserBalanceResponse` for checking account balance.

The project has no automated tests, but it can be compiled with `mvn package`. The examples module depends on `deepseek4j` and demonstrates features such as tool calling, structured outputs, and model/balance management.


## Maintenance
- Run `mvn package` to ensure the project builds. There are no tests.
- Keep this AGENTS.md file in sync with repository changes.
- Only update README.md if the information becomes outdated.

### Recent Changes
* Updated all example files to use the new DeepSeekClient API structure
* Fixed package names from `de.herbertwalde.app.deepseek_examples` to `de.entwicklertraining.deepseek4j.examples`
* Updated API calls to use new fluent structure: `client.chat().completion()`, `client.models()`, `client.user().balance()`
* Added Models and User Balance endpoints to AGENTS.md and README.md documentation

Wichtig: Aktualisiere AGENTS.md nach jedem Task.
Wichtig: Aktualisiere die README.md nach jedem Task nur wenn die Informationen darin veraltet sind
