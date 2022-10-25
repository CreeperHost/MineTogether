package net.creeperhost.minetogether.lib.chat.request;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import static net.creeperhost.minetogether.lib.web.WebConstants.MT_API;

/**
 * Created by covers1624 on 24/10/22.
 */
public class StatisticsRequest extends ApiRequest<StatisticsRequest.Response> {

    public StatisticsRequest() {
        super("GET", MT_API + "stats/all", Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response extends ApiResponse {

        public String servers = "unknown";
        public String connect = "unknown";
        public String users = "millions of";
        public String modpacks = "unknown";
        public String friends = "unknown";
        public String online = "thousands of";
    }
}
