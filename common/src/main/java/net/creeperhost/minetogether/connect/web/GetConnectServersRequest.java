package net.creeperhost.minetogether.connect.web;

import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.lib.web.ApiRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static net.creeperhost.minetogether.lib.web.WebConstants.MT;

/**
 * Created by covers1624 on 2/5/23.
 */
public class GetConnectServersRequest extends ApiRequest<List<GetConnectServersRequest.ConnectServer>> {

    private static final Type LIST_SERVERS = new TypeToken<List<ConnectServer>>() { }.getType();

    public GetConnectServersRequest() {
        super("GET", MT + "connectv2.json", LIST_SERVERS);
    }

    public static class ConnectServer {

        public String name;
        public String address;
        public int port;
        public boolean ssl;
        public List<String> publicKey;

        public static ConnectServer getLocalHost() {
            ConnectServer server = new ConnectServer();
            server.name = "localhost";
            server.address = "localhost";
            server.port = 32436;
            server.ssl = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ConnectServer.class.getResourceAsStream("/dev-pub.pem"), StandardCharsets.UTF_8))) {
                server.publicKey = reader.lines().toList();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return server;
        }
    }
}
