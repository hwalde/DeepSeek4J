# DeepSeek4J

DeepSeek4J is a fluent Java wrapper for the [DeepSeek API](https://ai.google.dev/deepseek-api/docs).
It builds on top of the lightweight [`api-base`](https://github.com/hwalde/api-base) library which
handles HTTP communication, authentication and exponential backoff. The goal is to provide a type safe
and convenient way to access DeepSeek services from Java, being as close to the raw API as possible.

> **A word from the author**
>
> I created this library because I was looking for a Java library that interacts with the DeepSeek API while staying as close to the raw API as possible—the official Java library does not. This implementation is fully compatible with DeepSeek but only includes the features I personally require. I maintain similar libraries for OpenAI and Gemini, each in its own repository so usage remains explicit. Everything supported by the Java API works here.
>
> At the moment the library only covers the parts I need. Chat Completions are implemented with nearly every option, but many specialized endpoints—like Fine Tuning or Evals—are missing. If you need additional functionality, feel free to implement it yourself or submit a pull request and I will consider adding it.


## Features

* Chat Completions including tool calling, structured outputs and vision inputs
* Vision capabilities for image understanding and analysis
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
    <artifactId>deepseek4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Usage

Instantiate a `DeepSeekClient` and use the builders exposed by its fluent API. The
[function calling example](deepseek4j-examples/src/main/java/de/entwicklertraining/deepseek4j/examples/DeepSeekChatCompletionWithFunctionCallingExample.java)
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

DeepSeekClient client = new DeepSeekClient(); // this will read the API key from the environment variable DEEPSEEK_API_KEY

DeepSeekChatCompletionResponse resp = client.chat().completion()
        .model("deepseek-chat")
        .addSystemMessage("You are a helpful assistant.")
        .addUserMessage("What's the weather in Berlin and the current time?")
        .addTool(weatherFunction)
        .execute();
System.out.println(resp.assistantMessage());
```

### Vision Example

The [vision example](deepseek4j-examples/src/main/java/de/entwicklertraining/deepseek4j/examples/DeepSeekChatCompletionWithVisionExample.java)
demonstrates image analysis capabilities:

```java
DeepSeekClient client = new DeepSeekClient();
DeepSeekChatCompletionResponse response = client.chat().completion()
        .model("deepseek-chat")
        .addUserMessage("What's in this image?")
        .addImageUrl("https://example.com/image.jpg")
        .execute();
System.out.println(response.assistantMessage());
```

### Models API Example

List all available models:

```java
DeepSeekClient client = new DeepSeekClient();
DeepSeekModelsResponse response = client.models().execute();

System.out.println("Available models: " + response.getModels().size());
response.getModels().forEach(model -> {
    System.out.println("Model ID: " + model.getId());
    System.out.println("Owned by: " + model.getOwnedBy());
});
```

### User Balance Example

Check your account balance:

```java
DeepSeekClient client = new DeepSeekClient();
DeepSeekUserBalanceResponse response = client.user().balance().execute();

System.out.println("Balance available: " + response.isAvailable());
response.getBalanceInfos().forEach(balance -> {
    System.out.println("Currency: " + balance.getCurrency());
    System.out.println("Total balance: " + balance.getTotalBalance());
    System.out.println("Granted balance: " + balance.getGrantedBalance());
    System.out.println("Topped-up balance: " + balance.getToppedUpBalance());
});
```

See the `deepseek4j-examples` module for more demonstrations including base64 images, structured outputs, and thinking mode.

### Configuring the Client

`DeepSeekClient` accepts an `ApiClientSettings` object for fine‑grained control over retries and timeouts. The API key can be configured directly and a hook can inspect each request before it is sent:

```java
ApiClientSettings settings = ApiClientSettings.builder()
        .setBearerAuthenticationKey("my api key")
        .beforeSend(req -> System.out.println("Sending " + req.getHttpMethod() + " " + req.getRelativeUrl()))
        .build();

DeepSeekClient client = new DeepSeekClient(settings);
```

## Project Structure

The library follows a clear structure:

* **`DeepSeekClient`** – entry point for all API calls. Extends `ApiClient` from *api-base*
  and registers error handling. Exposes endpoints via `chat()`, `models()`, and `user()`.
* **Request/Response classes** – located in packages like `chat.completion`, `models`, and `user.balance`.
  Each request extends `DeepSeekRequest` and has an inner `Builder` that extends
  `ApiRequestBuilderBase` from *api-base*. Responses extend `DeepSeekResponse`.
* **Tool calling** – defined via `DeepSeekToolDefinition` and handled by
  `DeepSeekToolsCallback` and `DeepSeekToolCallContext`.
* **Structured outputs** – use `DeepSeekJsonSchema` for defining response schemas.
* **Token utilities** – `DeepSeekTokenService` counts tokens.

The `deepseek4j-examples` module demonstrates various use cases and can be used as a quick start.

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
