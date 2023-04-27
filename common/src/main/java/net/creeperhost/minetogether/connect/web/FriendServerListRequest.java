package net.creeperhost.minetogether.connect.web;

import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by brandon3055 on 27/04/2023
 */
public class FriendServerListRequest extends ApiRequest<FriendServerListRequest.Response> {

    public FriendServerListRequest(String apiURL, String sessionToken) {
        super("GET", apiURL + "/api/v1/servers", Response.class);
        headers.add("x-session-token", sessionToken);
    }

    public static class Response extends ApiResponse {

        public final List<ServerEntry> servers = new ArrayList<>();

        public record ServerEntry(String friend, String serverToken) { }
    }
}
