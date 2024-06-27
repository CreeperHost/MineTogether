package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.lib.web.WebBody;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class PostLoginRequest extends ApiRequest<PostLoginRequest.Response> {

    public PostLoginRequest(String email, String password) {
        super("POST", CH + "json/account/login", PostLoginRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        body = WebBody.string("email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8) + "&", "application/x-www-form-urlencoded");
    }

    public static class Response extends ApiResponse {
        public String userid;
        public String currency;

        public Response(@Nullable String status, @Nullable String message) {
            super(status, message);
        }
    }
}
