package de.entwicklertraining.deepseek4j.user.balance;

import de.entwicklertraining.deepseek4j.DeepSeekResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a response from the DeepSeek User Balance API.
 * Provides methods to access the user's balance information.
 */
public final class DeepSeekUserBalanceResponse extends DeepSeekResponse<DeepSeekUserBalanceRequest> {

    /**
     * Constructs a DeepSeekUserBalanceResponse.
     *
     * @param json    The raw JSON response object.
     * @param request The original request that led to this response.
     */
    public DeepSeekUserBalanceResponse(JSONObject json, DeepSeekUserBalanceRequest request) {
        super(json, request);
    }

    /**
     * Gets whether the user's balance is sufficient for API calls.
     * @return true if the balance is available, false otherwise.
     */
    public boolean isAvailable() {
        return getJson().optBoolean("is_available", false);
    }

    /**
     * Gets the list of balance information for different currencies.
     * @return A list of BalanceInfo objects, or an empty list if not present or invalid.
     */
    public List<BalanceInfo> getBalanceInfos() {
        JSONArray balanceInfosArray = getJson().optJSONArray("balance_infos");
        if (balanceInfosArray == null) {
            return Collections.emptyList();
        }
        List<BalanceInfo> balanceInfos = new ArrayList<>();
        for (int i = 0; i < balanceInfosArray.length(); i++) {
            JSONObject balanceInfoJson = balanceInfosArray.optJSONObject(i);
            if (balanceInfoJson != null) {
                balanceInfos.add(new BalanceInfo(balanceInfoJson));
            }
        }
        return balanceInfos;
    }

    /**
     * Represents balance information for a specific currency.
     */
    public static class BalanceInfo {
        private final JSONObject json;

        BalanceInfo(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        /**
         * Gets the currency of the balance (CNY or USD).
         * @return The currency string, or null if not present.
         */
        public String getCurrency() {
            return json.optString("currency", null);
        }

        /**
         * Gets the total available balance, including the granted balance and the topped-up balance.
         * @return The total balance string, or null if not present.
         */
        public String getTotalBalance() {
            return json.optString("total_balance", null);
        }

        /**
         * Gets the total not expired granted balance.
         * @return The granted balance string, or null if not present.
         */
        public String getGrantedBalance() {
            return json.optString("granted_balance", null);
        }

        /**
         * Gets the total topped-up balance.
         * @return The topped-up balance string, or null if not present.
         */
        public String getToppedUpBalance() {
            return json.optString("topped_up_balance", null);
        }
    }
}