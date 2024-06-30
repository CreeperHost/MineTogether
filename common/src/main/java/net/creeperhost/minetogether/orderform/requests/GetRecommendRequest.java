package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.web.ApiRequest;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class GetRecommendRequest extends ApiRequest<GetRecommendRequest.Response> {

    public GetRecommendRequest(String version, int playerCount) {
        super("GET", CH + "json/order/mc/" + version + "/recommend/" + playerCount, GetRecommendRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response {
        public int recommended;
        public int ram;
    }
}
