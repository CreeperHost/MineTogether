package net.creeperhost.minetogether.serverlist.web;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.serverlist.data.ListType;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.util.ModPackInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by covers1624 on 25/10/22.
 */
public class GetServerListRequest extends ApiRequest<GetServerListRequest.Response> {

    public GetServerListRequest(ListType listType, String userHash) {
        super("PUT", CH_API + "serverlist/list", Response.class);

        Map<String, String> body = new HashMap<>();
        body.put("projectid", !ModPackInfo.base64FTBID.isEmpty() ? ModPackInfo.base64FTBID : ModPackInfo.curseID);
        body.put("listType", listType.name().toLowerCase());
        if (listType == ListType.INVITE) {
            body.put("hash", userHash);
        }

        jsonBody(body);
    }

    public static class Response extends ApiResponse {

        public List<Server> servers = new LinkedList<>();
    }
}
