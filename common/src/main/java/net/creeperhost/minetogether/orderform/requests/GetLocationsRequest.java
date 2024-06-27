package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.chat.request.v2.GetMeRequest;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;
import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class GetLocationsRequest extends ApiRequest<GetLocationsRequest.Response> {

    public GetLocationsRequest() {
        super("GET", CH + "json/locations", GetLocationsRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response {
        public Map<String, Integer> locMap = new HashMap<>();
        public Map<String, String> nameMap = new HashMap<>(); //Not Used
        public Map<String, Integer> regionMap = new HashMap<>(); //Not Used
    }
}
