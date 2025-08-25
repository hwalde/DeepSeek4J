package de.entwicklertraining.deepseek4j.examples;

import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.user.balance.DeepSeekUserBalanceResponse;

/**
 * Example demonstrating how to use the DeepSeek User Balance API to retrieve account balance information.
 */
public class DeepSeekUserBalanceExample {

    public static void main(String[] args) {
        DeepSeekClient client = new DeepSeekClient();
        
        // Create and execute the request to get user balance
        DeepSeekUserBalanceResponse response = client.user().balance()
                .execute();

        // Print whether the user's balance is sufficient for API calls
        System.out.println("Balance available for API calls: " + response.isAvailable());
        
        // Print the balance information for each currency
        System.out.println("\nBalance details:");
        response.getBalanceInfos().forEach(balanceInfo -> {
            System.out.println("-------------------");
            System.out.println("Currency: " + balanceInfo.getCurrency());
            System.out.println("Total balance: " + balanceInfo.getTotalBalance());
            System.out.println("Granted balance: " + balanceInfo.getGrantedBalance());
            System.out.println("Topped-up balance: " + balanceInfo.getToppedUpBalance());
        });
        
        // Print the total number of currencies
        System.out.println("\nTotal currencies: " + response.getBalanceInfos().size());
    }
}