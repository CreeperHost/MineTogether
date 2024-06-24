package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.lib.web.WebConstants;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class GetNameAvailableRequest extends ApiRequest<ApiResponse> {

    public GetNameAvailableRequest(String name) {
        super("GET", WebConstants.CH + "json/availability/" + name, ApiResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }
}
