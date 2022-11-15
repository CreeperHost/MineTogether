package net.creeperhost.minetogether.connect.web;

import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.lib.web.ApiRequest;

import java.lang.reflect.Type;
import java.util.List;

import static net.creeperhost.minetogether.lib.web.WebConstants.MT;

/**
 * Created by covers1624 on 15/11/22.
 */
public class GetConnectServersRequest extends ApiRequest<List<GetConnectServersRequest.Server>> {

    private static final Type LIST_SERVERS = new TypeToken<List<Server>>() { }.getType();

    public GetConnectServersRequest() {
        super("GET", MT + "connect.json", LIST_SERVERS);
    }

    public static class Server {

        public String name;
        public String address;
        public String httpProtocol;
        public int httpPort;
    }
}
