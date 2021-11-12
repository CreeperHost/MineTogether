package net.creeperhost.minetogetherconnect;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import net.creeperhost.minetogetherconnect.LibraryHacks.WebUtils;

public class ConnectMain {
    static BackendServer backendServer;
    static Gson gson = new Gson();
    static String authStr = "";
    public static void main(String[] args) {
        // for now pass it in, in the future we'll make the server do Mojang auth, hand us a value
        authStr = args[0];
        listen((success, msg) -> {
            if (!success) {
                System.out.println("Error whilst opening to friends: " + msg);
            } else {
                System.out.println("Opened to friends successfully!");
            }
        });
    }

    public static boolean listen(BiConsumer<Boolean, String> callback)
    {
        BackendServer backendServer = getBackendServer();
        backendServer.openToOthers(callback);
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

        public void openToOthers(BiConsumer<Boolean, String> callback) {
            String register = buildUrl("register");
            HashMap<String, String> bodyArr = new HashMap<>();
            bodyArr.put("auth", authStr);
            String bodyString = gson.toJson(bodyArr);
            String response = WebUtils.putWebResponse(register, bodyString, true, false);
            if (response.equals("error")) {
                callback.accept(false, "unknown");
            }
            RegisterResponse registerResponse = gson.fromJson(response, RegisterResponse.class);
            if (!registerResponse.success) {
                callback.accept(false, registerResponse.message);
            }

            int readBytes = 0;

            boolean success = false;

            Socket socket = null;
            InputStream inputStream = null;
            InputStreamReader inputReader = null;
            OutputStream outputStream = null;

            ConnectUtil connectUtil;

            try {
                SocketChannel localSocketChannel = SocketChannel.open(new InetSocketAddress(this.address, registerResponse.port - 1));
                socket = localSocketChannel.socket();
                inputStream = socket.getInputStream();
                inputReader = new InputStreamReader(inputStream);

                outputStream = socket.getOutputStream();
                outputStream.write((registerResponse.secret + "\n").getBytes(StandardCharsets.UTF_8));

                long startTime = System.currentTimeMillis();

                connectUtil = new ConnectUtil(inputReader);

                socket.setSoTimeout(5000);
                while (socket.isConnected()) {
                    long now = System.currentTimeMillis();
                    if(now - startTime > 15000) {
                        callback.accept(false, "timeout");
                        return;
                    }
                    String line = connectUtil.readLine();
                    if (line == null) {
                        ConnectUtil.CloseMultiple(inputReader, inputStream, outputStream, socket);
                        callback.accept(false, "closed");
                        return;
                    }

                    if (line.equals("PING")) {
                        outputStream.write("PONG\n".getBytes(StandardCharsets.UTF_8));
                    } else if (line.equals("OK")) {
                        success = true;
                        break;
                    } else {
                        callback.accept(false, line);
                        return;
                    }
                }
            } catch (Exception e) {
                ConnectUtil.CloseMultiple(inputReader, inputStream, outputStream, socket);
                callback.accept(false, "timeout");
                return;
            }

            if (!success) {
                ConnectUtil.CloseMultiple(inputReader, inputStream, outputStream, socket);
                callback.accept(false, "unknown");
                return;
            }


            // TODO: continue with managing control socket
            try {
                socket.setSoTimeout(20000);
                while (socket.isConnected()) {
                    String line = connectUtil.readLine();
                    if (line == null) {
                        ConnectUtil.CloseMultiple(inputReader, inputStream, outputStream, socket);
                        return;
                    }

                    switch(line) {
                        case "PING":
                            outputStream.write("PONG\n".getBytes(StandardCharsets.UTF_8));
                            break;
                        case "Connect":
                            ProxyHandler.Start(42069, this.address, registerResponse.port - 1, registerResponse.secret, callback);
                    }
                }
            } catch (Exception e) {
                callback.accept(false, "closed");
                ConnectUtil.CloseMultiple(inputReader, inputStream, outputStream, socket);
            }

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
