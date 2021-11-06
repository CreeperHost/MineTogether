package net.creeperhost.minetogetherconnect;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.creeperhost.minetogetherconnect.LibraryHacks.WebUtils;

public class ConnectMain {
    static BackendServer backendServer;
    static Gson gson = new Gson();
    static String authStr = "";
    public static void main(String[] args) {
        // for now pass it in, in the future we'll make the server do Mojang auth, hand us a value
        authStr = args[0];
        listen();
    }

    public static boolean listen()
    {
        BackendServer backendServer = getBackendServer();
        backendServer.openToOthers();
        return true;
    }

    public static BackendServer getBackendServer() {
        if (backendServer != null) return backendServer;
        String webResponse = WebUtils.getWebResponse("https://minetogether.io/connect.json");
        if (webResponse.equals("error")) {
            return BackendServer.getDefault();
        }

        Type listOfMyClassObject = new TypeToken<ArrayList<BackendServer>>() {}.getType();

        // maybe tries just in case
        List<BackendServer> servers = gson.fromJson(webResponse, listOfMyClassObject);
        
        if (servers == null) {
            return BackendServer.getDefault();
        }

        String closestString = WebUtils.getWebResponse("https://creeperhost.net/json/datacentre/closest");

        if (closestString.equals("error")) {
            return BackendServer.getDefault();
        }

        ClosestResponse closestResponse = gson.fromJson(closestString, ClosestResponse.class);

        if (closestResponse == null || closestResponse.datacentre == null) {
            return BackendServer.getDefault();
        }

        BackendServer chosenServer = null;

        for (BackendServer server: servers) {
            if (server.name.toLowerCase(Locale.ROOT).equals(closestResponse.datacentre.name.toLowerCase(Locale.ROOT))) {
                chosenServer = server;
                break;
            }
        }

        if (chosenServer == null) {
            return BackendServer.getDefault();
        }

        return chosenServer;
    }

    public static class BackendServer {
        private String name;
        private String address;
        private String httpProtocol;
        private int httpPort;
        private String baseURL = null;

        private BackendServer(String name, String address, String httpProtocol, int httpPort) {
            this.name = name;
            this.address = address;
            this.httpProtocol = httpProtocol;
            this.httpPort = httpPort;
        }

        private static BackendServer getDefault() {
            return new BackendServer("Grantham", "ghm.connect.minetogether.ch.tools", "http", 8080);
        }

        public boolean openToOthers() {
            String register = buildUrl("register");
            HashMap<String, String> bodyArr = new HashMap<>();
            bodyArr.put("auth", authStr);
            String bodyString = gson.toJson(bodyArr);
            String response = WebUtils.putWebResponse(register, bodyString, true, false);
            if (response.equals("error")) {
                return false;
            }
            RegisterResponse registerResponse = gson.fromJson(response, RegisterResponse.class);
            if (registerResponse.success) {

            }
            // TODO: continue with opening to control socket
            return true;
        }

        private String buildUrl(String method) {
            StringBuilder builder = new StringBuilder();

            if(baseURL == null)
            {
                builder.append(httpProtocol).append("://");
                builder.append(address).append(":");
                builder.append(httpPort);
                builder.append("/");
                baseURL = builder.toString();
            } else {
                builder.append(baseURL);
            }

            builder.append(method);

            return builder.toString();
        }
    }

    public static class ClosestResponse {
        private Datacentre datacentre;

        public static class Datacentre {
            private String name;
        }
    }

    public static class RegisterResponse {
        private boolean success;
        private String message;
        private int port;
        private String secret;
    }
}
