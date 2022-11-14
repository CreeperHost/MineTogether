package net.creeperhost.minetogether.lib.chat.request;

import net.creeperhost.minetogether.lib.web.ApiRequest;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 21/6/22.
 */
public class IRCServerListRequest extends ApiRequest<IRCServerListResponse> {

    public IRCServerListRequest() {
        super("GET", CH_API + "minetogether/chatserver", IRCServerListResponse.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }
}
