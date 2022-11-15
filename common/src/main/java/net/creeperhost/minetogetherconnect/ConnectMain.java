package net.creeperhost.minetogetherconnect;

import com.google.gson.Gson;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.web.GetConnectServersRequest;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.util.GetClosestDCRequest;
import net.creeperhost.minetogetherconnect.LibraryHacks.WebUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConnectMain {

    private static final Logger LOGGER = LogManager.getLogger();

    public static int maxPlayerCount = 3;
    static BackendServer backendServer;
    static Gson gson = new Gson();
    static String authStr = "";
    public static String authError = "";
    static String messageStr = new String(Character.toChars(0x1F4A9));
    private static SocketChannel localSocketChannel;
    private static Socket socket;
    private static InputStream inputStream;
    private static InputStreamReader inputReader;
    private static OutputStream outputStream;

    public static boolean listen(BiConsumer<Boolean, String> callback, Consumer<String> messageRelayer) {
        getBackendServer().openToOthers(callback, messageRelayer);
        return true;
    }

    public static BackendServer getBackendServer() {
        if (backendServer == null) {
            backendServer = computeServer();
        }
        return backendServer;
    }

    private static BackendServer computeServer() {
        try {
            ApiClientResponse<List<GetConnectServersRequest.Server>> connectServersResponse = MineTogether.API.execute(new GetConnectServersRequest());
            if (!connectServersResponse.hasBody()) {
                LOGGER.error("Failed to get available MineTogether Connect server list.");
                return BackendServer.getDefault();
            }

            List<GetConnectServersRequest.Server> connectServers = connectServersResponse.apiResponse();

            ApiClientResponse<GetClosestDCRequest.Response> closestDCResponse = MineTogether.API.execute(new GetClosestDCRequest());
            if (!closestDCResponse.hasBody()) {
                LOGGER.error("Failed to get Closest DC locations.");
                return BackendServer.getDefault();
            }

            for (GetClosestDCRequest.DataCenter dc : closestDCResponse.apiResponse().getDataCenters()) {
                for (GetConnectServersRequest.Server server : connectServers) {
                    if (dc.getName().replace(" ", "").equalsIgnoreCase(server.name.replace(" ", ""))) {
                        LOGGER.info("Selected server {}. Closest DC was {}.", server.name, dc.getName());
                        return new BackendServer(server.name, server.address, server.httpProtocol, server.httpPort);
                    }
                }
            }

            LOGGER.info("Could not select a server. Using default.");
        } catch (IOException ex) {
            LOGGER.error("Error selecting server. Using default.", ex);
        }
        return BackendServer.getDefault();
    }

    public static boolean doAuth() {
        return getBackendServer().doAuth();
    }

    public static void close() {
        ConnectUtil.CloseMultiple(localSocketChannel, socket, inputReader, inputStream, outputStream);
    }

    public static class BackendServer {

        private String name;
        public String address;
        private String httpProtocol;
        private int httpPort;
        private String baseURL = null;

        private BackendServer(String name, String address, String httpProtocol, int httpPort) {
            this.name = name;
            this.address = address;
            this.httpProtocol = httpProtocol;
            this.httpPort = httpPort;
        }

        private static BackendServer getLocal() {
            return new BackendServer("localhost", "127.0.0.1", "http", 8080);
        }

        private static BackendServer getDefault() {
            return new BackendServer("Grantham", "connect.ghm.minetogether.ch.tools", "https", 443);
        }

        private boolean doAuth() {
            if (!authStr.isEmpty()) return true;
            String auth = buildUrl("auth");
            HashMap<String, String> bodyArr = new HashMap<>();
            bodyArr.put("auth", MineTogetherChat.CHAT_AUTH.getUUID() + ":" + MineTogetherChat.CHAT_AUTH.beginMojangAuth());
            bodyArr.put("type", "minecraft");
            String bodyString = gson.toJson(bodyArr);
            String response = WebUtils.putWebResponse(auth, bodyString, true, false);
            authError = "Unknown";
            if (response.equals("error")) {
                return false;
            }
            AuthResponse authResponse = fromJsonWrapper(response, AuthResponse.class);
            if (authResponse == null) {
                return false;
            }

            if (!authResponse.success) {
                authError = authResponse.message;
                return false;
            }

            authError = "";

            authStr = authResponse.authSecret;
            return true;
        }

        public void openToOthers(BiConsumer<Boolean, String> callback, Consumer<String> messageRelayer) {
            LOGGER.info("MineTogether Connect: Opening to others.");
            if (!doAuth()) {
                callback.accept(false, "failed auth");
                return;
            }
            String register = buildUrl("register");
            HashMap<String, String> bodyArr = new HashMap<>();
            bodyArr.put("auth", authStr);
            String bodyString = gson.toJson(bodyArr);
            String response = WebUtils.putWebResponse(register, bodyString, true, false);
            if (response.equals("error")) {
                callback.accept(false, "unknown");
            }
            RegisterResponse registerResponse = fromJsonWrapper(response, RegisterResponse.class);
            if (registerResponse == null || !registerResponse.success) {
                callback.accept(false, registerResponse == null ? "unknown" : registerResponse.message);
            }

            LOGGER.info("MineTogether Connect: Register to server succeeded");

            int readBytes = 0;

            boolean success = false;

            ConnectUtil connectUtil;

            try {
                localSocketChannel = SocketChannel.open(new InetSocketAddress(this.address, registerResponse.port - 1));
                if (registerResponse.maxPlayers != -1) maxPlayerCount = registerResponse.maxPlayers;
                socket = localSocketChannel.socket();
                inputStream = socket.getInputStream();
                inputReader = new InputStreamReader(inputStream);

                LOGGER.info("MineTogether Connect: Connected to control socket");

                outputStream = socket.getOutputStream();
                outputStream.write((registerResponse.secret + "\n").getBytes(StandardCharsets.UTF_8));

                long startTime = System.currentTimeMillis();

                connectUtil = new ConnectUtil(inputReader);

                socket.setSoTimeout(5000);
                while (socket.isConnected()) {
                    long now = System.currentTimeMillis();
                    if (now - startTime > 15000) {
                        callback.accept(false, "timeout");
                        return;
                    }
                    String line = connectUtil.readLine();
                    if (line == null) {
                        close();
                        callback.accept(false, "closed");
                        return;
                    }

                    if (line.equals("PING")) {
                        outputStream.write("PONG\n".getBytes(StandardCharsets.UTF_8));
                    } else if (line.equals("OK")) {
                        LOGGER.info("MineTogether Connect: Authed to control socket successfully");
                        success = true;
                        break;
                    } else {
                        callback.accept(false, line);
                        return;
                    }
                }
            } catch (Exception e) {
                close();
                callback.accept(false, "timeout");
                LOGGER.error("MineTogether Connect control socket failure.", e);
                return;
            }

            if (!success) {
                close();
                callback.accept(false, "unknown");
                return;
            }

            callback.accept(true, ""); // READY
            LOGGER.info("MineTogether Connect: Now handling control loop, ready for connections");

            try {
                socket.setSoTimeout(20000);
                while (socket.isConnected()) {
                    String line = connectUtil.readLine();
                    if (line == null) {
                        close();
                        return;
                    }

                    switch (line) {
                        case "PING":
                            outputStream.write("PONG\n".getBytes(StandardCharsets.UTF_8));
                            break;
                        case "Connect":
                            LOGGER.info("MineTogether Connect: Handling connect: {}, {}", address, registerResponse.port - 1);
                            ProxyHandler.Start(42069, this.address, registerResponse.port - 1, registerResponse.secret, callback);
                            break;
                        default:
                            if (line.startsWith(messageStr)) {
                                messageStr = line.substring(messageStr.length());
                                messageRelayer.accept(messageStr);
                                // print message to chat
                            }
                    }
                }
            } catch (Exception e) {
                messageRelayer.accept("CLOSED123");
                callback.accept(false, "closed");
                LOGGER.error("Fatal exception in control loop.", e);
                ConnectMain.close();
            }
        }

        public ConnectHandler.FriendsResponse getFriends() {
            if (!doAuth()) {
                return null;
            }
            String register = buildUrl("getFriendsPorts");
            HashMap<String, String> bodyArr = new HashMap<>();
            bodyArr.put("auth", authStr);
            String bodyString = gson.toJson(bodyArr);
            String response = WebUtils.putWebResponse(register, bodyString, true, false);
            if (response.equals("error")) {
                return null;
            }
            ConnectHandler.FriendsResponse friendsResponse = gson.fromJson(response, ConnectHandler.FriendsResponse.class);
            if (!friendsResponse.isSuccess()) {
                return null;
            }

            return friendsResponse;
        }

        private String buildUrl(String method) {
            StringBuilder builder = new StringBuilder();

            if (baseURL == null) {
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

    private static <T> T fromJsonWrapper(String data, Class<T> clazz) {
        try {
            return gson.fromJson(data, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static class BaseResponse {

        boolean success;
        String message;
    }

    public static class RegisterResponse extends BaseResponse {

        public int maxPlayers = -1;
        private int port;
        private String secret;
    }

    public static class AuthResponse extends BaseResponse {

        private String authSecret;
    }
}
