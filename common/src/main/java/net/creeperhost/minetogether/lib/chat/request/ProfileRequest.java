package net.creeperhost.minetogether.lib.chat.request;

import com.google.common.collect.ImmutableMap;
import net.creeperhost.minetogether.lib.web.ApiRequest;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 21/6/22.
 */
public class ProfileRequest extends ApiRequest<ProfileResponse> {

    public ProfileRequest(String target) {
        super("PUT", CH_API + "minetogether/profile", ProfileResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");

        jsonBody(ImmutableMap.of("target", target), STRING_MAP_TYPE);
    }
}
