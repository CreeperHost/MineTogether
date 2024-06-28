package net.creeperhost.minetogether.orderform.requests;

import net.covers1624.quack.net.httpapi.WebBody;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 27/06/2024
 */
public class PostEmailExistsRequest extends ApiRequest<ApiResponse> {

    public PostEmailExistsRequest(String email) {
        super("POST", CH + "json/account/exists", ApiResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");


        body = WebBody.string("email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&", "application/x-www-form-urlencoded");
    }
}
