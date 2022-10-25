package net.creeperhost.minetogether.server.web;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ServerListUpdateRequest extends ApiRequest<ServerListUpdateRequest.Response> {

    public ServerListUpdateRequest(String serverIp, String secret, String name, String projectId, String port, boolean inviteOnly) {
        super("PUT", CH_API + "serverlist/update", Response.class);

        Map<String, String> body = new HashMap<>();
        if (!serverIp.isEmpty()) {
            body.put("ip", serverIp);
        }
        if (StringUtils.isNotEmpty(secret)) {
            body.put("secret", secret);
        }
        body.put("name", name);
        body.put("projectid", projectId);
        body.put("port", port);
        body.put("invite-only", inviteOnly ? "1" : "0");
        body.put("version", "2");
        jsonBody(body);
    }

    public static class Response extends ApiResponse {

        public int id;
        @Nullable
        public String secret;
    }
}
