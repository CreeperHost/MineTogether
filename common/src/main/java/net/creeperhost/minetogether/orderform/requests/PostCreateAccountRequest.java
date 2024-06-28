package net.creeperhost.minetogether.orderform.requests;

import net.covers1624.quack.net.httpapi.WebBody;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class PostCreateAccountRequest extends ApiRequest<PostCreateAccountRequest.Response> {

    public PostCreateAccountRequest(Order order) {
        super("POST", CH + "json/account/create", PostCreateAccountRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        StringBuilder builder = new StringBuilder();
        builder.append("servername=").append(URLEncoder.encode(order.name, UTF_8)).append("&");
        builder.append("modpack=").append(URLEncoder.encode(ModPackInfo.getInfo().curseID, UTF_8)).append("&");
        builder.append("email=").append(URLEncoder.encode(order.emailAddress, UTF_8)).append("&");
        builder.append("password=").append(URLEncoder.encode(order.password, UTF_8)).append("&");
        builder.append("fname=").append(URLEncoder.encode(order.firstName, UTF_8)).append("&");
        builder.append("lname=").append(URLEncoder.encode(order.lastName, UTF_8)).append("&");
        builder.append("addr1=").append(URLEncoder.encode(order.address, UTF_8)).append("&");
        builder.append("city=").append(URLEncoder.encode(order.city, UTF_8)).append("&");
        builder.append("tel=").append(URLEncoder.encode(order.phone, UTF_8)).append("&");
        builder.append("county=").append(URLEncoder.encode(order.state, UTF_8)).append("&");
        builder.append("state=").append(URLEncoder.encode(order.state, UTF_8)).append("&");
        builder.append("country=").append(URLEncoder.encode(order.country, UTF_8)).append("&");
        builder.append("pcode=").append(URLEncoder.encode(order.zip, UTF_8)).append("&");
        builder.append("currency=").append(URLEncoder.encode(order.currency, UTF_8)).append("&");
        body = WebBody.string(builder.toString(), "application/x-www-form-urlencoded");
    }

    public static class Response extends ApiResponse {
        public String currency;
        public String userid;

        public Response(@Nullable String status, @Nullable String message) {
            super(status, message);
        }
    }
}
