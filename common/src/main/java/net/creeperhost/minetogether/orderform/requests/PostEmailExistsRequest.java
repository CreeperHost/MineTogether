package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.lib.web.WebUtils.UrlParamPair;

import java.util.ArrayList;
import java.util.List;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 27/06/2024
 */
public class PostEmailExistsRequest extends ApiRequest<ApiResponse> {

    public PostEmailExistsRequest(String email) {
        super("POST", CH + "json/account/exists", ApiResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        List<UrlParamPair> entries = new ArrayList<>();
        entries.add(UrlParamPair.of("email", email));
        formBody(entries);
    }
}
