package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.models.DeepSeekModelsResponse;

/**
 * Example demonstrating how to use the DeepSeek Models API to list available models.
 */
public class DeepSeekModelsExample {

    public static void main(String[] args) {
        DeepSeekClient client = new DeepSeekClient();
        
        // Create and execute the request to list models
        DeepSeekModelsResponse response = client.models()
                .execute();

        // Print the response object type (should be "list")
        System.out.println("Response object type: " + response.getObject());
        
        // Print the list of available models
        System.out.println("\nAvailable models:");
        response.getModels().forEach(model -> {
            System.out.println("-------------------");
            System.out.println("ID: " + model.getId());
            System.out.println("Object: " + model.getObject());
            System.out.println("Owned by: " + model.getOwnedBy());
        });
        
        // Print the total number of models
        System.out.println("\nTotal models available: " + response.getModels().size());
    }
}