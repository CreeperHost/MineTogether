package net.creeperhost.minetogether.util;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by covers1624 on 25/10/22.
 */
public class GetCurseForgeVersionRequest extends ApiRequest<GetCurseForgeVersionRequest.Response> {

    public GetCurseForgeVersionRequest(String base64) {
        super("GET", CH + "json/modpacks/modpacksch/" + base64, Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response extends ApiResponse {

        public String id = "";
    }
}
