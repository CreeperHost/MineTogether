package net.creeperhost.minetogether.orderform.requests;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.lib.web.WebUtils.UrlParamPair;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.ProfileKeyPair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class PostOrderRequest extends ApiRequest<PostOrderRequest.Response> {

    public PostOrderRequest(Order order, String dcId, String pregen, String fallbackName) {
        super("POST", CH + "json/order/" + order.clientID + "/" + order.productID + "/" + dcId, PostOrderRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        List<UrlParamPair> entries = new ArrayList<>();
        entries.add(UrlParamPair.of("name", order.name));
        entries.add(UrlParamPair.of("swid", ModPackInfo.getInfo().websiteID));

        if (order.pregen) {
            entries.add(UrlParamPair.of("pregen", pregen));
        }
        if (!StringUtil.isNullOrEmpty(order.worldUrl)) {
            entries.add(UrlParamPair.of("worldUrl", order.worldUrl));
        }
        if (!StringUtil.isNullOrEmpty(fallbackName)) {
            entries.add(UrlParamPair.of("fallback", fallbackName));
        }

        entries.add(UrlParamPair.of("ownerUuid", Minecraft.getInstance().getUser().getProfileId().toString()));

        try {
            CompletableFuture<Optional<ProfileKeyPair>> pair = Minecraft.getInstance().getProfileKeyPairManager().prepareKeyPair();
            while (!pair.isDone())
            {
                Thread.sleep(100);
            }

            if(pair.get().isPresent())
            {
                String auth = Base64.getEncoder().encodeToString(pair.get().get().publicKey().data().key().getEncoded());
                entries.add(UrlParamPair.of("pubkey", auth));
            }
        } catch (InterruptedException | ExecutionException ignored) {}

        formBody(entries);
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
