package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.lib.web.WebBody;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class PostOrderRequest extends ApiRequest<PostOrderRequest.Response> {

    public PostOrderRequest(Order order, String dcId, String pregen, String fallbackName) {
        super("POST", CH + "json/order/" + order.clientID + "/" + order.productID + "/" + dcId, PostOrderRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        StringBuilder builder = new StringBuilder();
        builder.append("name=").append(URLEncoder.encode(order.name, UTF_8)).append("&");
        builder.append("swid=").append(URLEncoder.encode(ModPackInfo.getInfo().websiteID, UTF_8)).append("&");

        if (order.pregen) {
            builder.append("pregen=").append(URLEncoder.encode(pregen, UTF_8)).append("&");
        }
        if (!StringUtil.isNullOrEmpty(order.worldUrl)) {
            builder.append("worldUrl=").append(URLEncoder.encode(order.worldUrl, UTF_8)).append("&");
        }
        if (!StringUtil.isNullOrEmpty(fallbackName)) {
            builder.append("fallback=").append(URLEncoder.encode(fallbackName, UTF_8)).append("&");
        }

        body = WebBody.string(builder.toString(), "application/x-www-form-urlencoded");
    }

    public static class Response extends ApiResponse {
        public Data more;

        public Response(@Nullable String status, @Nullable String message) {
            super(status, message);
        }
    }

    public static class Data {
        public String invoiceid;
        public String orderid;
    }
}
