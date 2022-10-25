package net.creeperhost.minetogether.server.web;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import java.util.List;
import java.util.Map;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 25/10/22.
 */
public class SendInviteRequest extends ApiRequest<ApiResponse> {

    public SendInviteRequest(int id, List<String> hash) {
        super("PUT", CH_API + "serverlist/invite", ApiResponse.class);

        jsonBody(Map.of(
                "id", String.valueOf(id),
                "hash", hash
        ));
    }
}
