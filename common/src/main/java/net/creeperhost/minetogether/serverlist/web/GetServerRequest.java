package net.creeperhost.minetogether.serverlist.web;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.serverlist.data.Server;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 26/10/22.
 */
public class GetServerRequest extends ApiRequest<GetServerRequest.Response> {

    public GetServerRequest(String serverId) {
        super("PUT", CH_API + "serverlist/server", Response.class);

        jsonBody(Map.of("serverid", serverId));
    }

    public static class Response extends ApiResponse {

        @Nullable
        public Server server;
    }
}
