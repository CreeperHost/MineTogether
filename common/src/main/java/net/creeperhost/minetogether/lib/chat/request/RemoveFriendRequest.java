package net.creeperhost.minetogether.lib.chat.request;

import com.google.common.collect.ImmutableMap;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 7/9/22.
 */
public class RemoveFriendRequest extends ApiRequest<ApiResponse> {

    public RemoveFriendRequest(String friendCode, String ourHash) {
        super("PUT", CH_API + "serverlist/removefriend", ApiResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        jsonBody(ImmutableMap.of(
                "hash", ourHash,
                "target", friendCode
        ), STRING_MAP_TYPE);
    }
}
