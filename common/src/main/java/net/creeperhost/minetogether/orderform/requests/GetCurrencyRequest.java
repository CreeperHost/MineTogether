package net.creeperhost.minetogether.orderform.requests;

import com.google.gson.annotations.SerializedName;
import net.creeperhost.minetogether.lib.web.ApiRequest;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class GetCurrencyRequest extends ApiRequest<GetCurrencyRequest.Response> {

    public GetCurrencyRequest(String currency) {
        super("GET", CH + "json/currency/" + currency, GetCurrencyRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response {
        public String id;
        public String code;
        public String prefix;
        public String suffix;
        public String format;
        public String rate;
        @SerializedName ("default")
        public String _default;
    }
}
