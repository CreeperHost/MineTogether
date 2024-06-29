package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.lib.web.WebUtils.UrlParamPair;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.util.ModPackInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class PostCreateAccountRequest extends ApiRequest<PostCreateAccountRequest.Response> {

    public PostCreateAccountRequest(Order order) {
        super("POST", CH + "json/account/create", PostCreateAccountRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        List<UrlParamPair> entries = new ArrayList<>();
        entries.add(UrlParamPair.of("servername", order.name));
        entries.add(UrlParamPair.of("modpack", ModPackInfo.getInfo().curseID));
        entries.add(UrlParamPair.of("email", order.emailAddress));
        entries.add(UrlParamPair.of("password", order.password));
        entries.add(UrlParamPair.of("fname", order.firstName));
        entries.add(UrlParamPair.of("lname", order.lastName));
        entries.add(UrlParamPair.of("addr1", order.address));
        entries.add(UrlParamPair.of("city", order.city));
        entries.add(UrlParamPair.of("tel", order.phone));
        entries.add(UrlParamPair.of("county", order.state));
        entries.add(UrlParamPair.of("state", order.state));
        entries.add(UrlParamPair.of("country", order.country));
        entries.add(UrlParamPair.of("pcode", order.zip));
        entries.add(UrlParamPair.of("currency", order.currency));
        formBody(entries);
    }

    public static class Response extends ApiResponse {
        public String currency;
        public String userid;

        public Response(@Nullable String status, @Nullable String message) {
            super(status, message);
        }
    }
}
