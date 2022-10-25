package net.creeperhost.minetogether.lib.chat.request;

import com.google.common.collect.ImmutableMap;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 21/9/22.
 */
public class AddFriendRequest extends ApiRequest<ApiResponse> {

    public AddFriendRequest(String ourHash, String friendCode, String desiredName) {
        super("PUT", CH_API + "serverlist/requestfriend", ApiResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        jsonBody(GSON, ImmutableMap.of(
                "hash", ourHash,
                "target", friendCode,
                "display", desiredName
        ));
    }
}
