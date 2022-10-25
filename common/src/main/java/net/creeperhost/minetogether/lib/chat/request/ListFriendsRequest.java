package net.creeperhost.minetogether.lib.chat.request;

import com.google.common.collect.ImmutableMap;
import net.creeperhost.minetogether.lib.web.ApiRequest;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 7/9/22.
 */
public class ListFriendsRequest extends ApiRequest<ListFriendsResponse> {

    public ListFriendsRequest(String target) {
        super("PUT", CH_API + "serverlist/listfriend", ListFriendsResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        jsonBody(ImmutableMap.of("hash", target), STRING_MAP_TYPE);
    }
}
