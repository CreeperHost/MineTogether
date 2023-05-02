package net.creeperhost.minetogether.connect.data;

import net.creeperhost.minetogether.connect.ConnectHost;
import net.creeperhost.minetogether.connect.http.data.HostListResponse;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.session.JWebToken;

/**
 * Created by covers1624 on 2/5/23.
 */
public class HostListRequest extends ApiRequest<HostListResponse> {

    public HostListRequest(JWebToken token) {
        super("GET", "http://localhost:32436/api/v1/hosts", HostListResponse.class);

        headers.add("X-Session-Token", token.toString());
    }
}
