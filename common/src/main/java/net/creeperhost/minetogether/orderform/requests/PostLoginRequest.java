package net.creeperhost.minetogether.orderform.requests;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class PostLoginRequest extends ApiRequest<PostLoginRequest.Response> {

    public PostLoginRequest(String email, String password) {
        super("POST", CH + "json/account/login", PostLoginRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");



//        jsonBody(GSON, ImmutableMap.of(
//                "email", email,
//                "password", password
//        ));
    }

    public static class Response extends ApiResponse {

    }


}
