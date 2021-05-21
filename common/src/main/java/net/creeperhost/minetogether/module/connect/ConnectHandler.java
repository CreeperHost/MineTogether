package net.creeperhost.minetogether.module.connect;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ConnectHandler {
    private static final HashMap<Integer, ResponseInfo> awaiting = new HashMap<>();
    private static final Object lock = new Object();
    private static final Gson gson = new Gson();
    private static Socket socket = null;

    public static void connectToProc() {
        try {
            socket = new Socket("127.0.0.1", 42068);
            ConnectHelper.isEnabled = true;
            CompletableFuture.runAsync(() -> {
                InputStream inputStream = null;
                InputStreamReader ir = null;
                BufferedReader in = null;
                try {
                    inputStream = socket.getInputStream();
                    ir = new InputStreamReader(inputStream);
                    in = new BufferedReader(ir);

                    while (socket.isConnected()) {
                        String str = in.readLine();
                        Response msg = gson.fromJson(str, Response.class);
                        synchronized (lock) {
                            int msgId = msg.getId();
                            if(awaiting.containsKey(msgId)) {
                                ResponseInfo<Response> responseInfo = awaiting.get(msgId);
                                Response newMsg = gson.fromJson(str, responseInfo.getClazz());
                                responseInfo.getCallback().apply(newMsg);
                                awaiting.remove(msgId);
                            } else {
                                // unexpected message
                            }
                        }
                    }
                } catch (Throwable ignored) {
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                        if (ir != null) ir.close();
                        if (in != null) ir.close();
                        socket.close();
                        socket = null;
                        ConnectHelper.isEnabled = false;
                    } catch (IOException ignored) {

                    }
                }
            });
        } catch (IOException e) {
            try {
                if (socket != null) socket.close();
                socket = null;
            } catch (IOException ignored) {
            }
            e.printStackTrace();
            ConnectHelper.isEnabled = false;
        }
    }

    public static boolean sendMessage(Message message, Function<Response, Void> callback) {
        return sendMessage(message, new ResponseInfo(Response.class, callback));
    }

    public static boolean sendMessage(Message message, ResponseInfo callback) {
        if (socket == null) return false;
        synchronized (lock) {
            OutputStream outputStream = null;
            try {
                outputStream = socket.getOutputStream();
                String s = gson.toJson(message);
                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                outputStream.write(bytes);
                outputStream.write('\n');
                if (callback != null) {
                    awaiting.put(message.id, callback);
                }
                return true;
            } catch (Throwable t) {
                return false;
            }
        }

    }

    public static Response openBlocking() {
        return blocking(ConnectHandler::open);
    }

    public static Void open(Function<Response, Void> callback) {
        sendMessage(new Message("OPEN"), callback);
        return null;
    }

    public static <E extends Response> E blocking(Function<Function<E, Void>, Void> func) {

        AtomicReference<E> tempResponse = new AtomicReference<>();
        final Object tempLock = new Object();
        func.apply((response) -> {
            tempResponse.set(response);
            synchronized (tempLock) {
                tempLock.notifyAll();
            }

            return null;
        });

        try {
            synchronized (tempLock) {
                tempLock.wait();
            }
        } catch (InterruptedException ignored) {
        }

        return tempResponse.get();
    }

    public static void close() {
        sendMessage(new Message("CLOSE"), (ResponseInfo) null);
    }

    public static Void getFriends(Function<FriendsResponse, Void> callback) {
        sendMessage(new Message("FRIENDS"), new ResponseInfo<>(FriendsResponse.class, callback));
        return null;
    }

    public static FriendsResponse getFriendsBlocking() {
        return blocking(ConnectHandler::getFriends);
    }

    static class Response extends Message {
        private boolean success;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    static class FriendsResponse extends Response {
        private ArrayList<Friend> friends;

        public ArrayList<Friend> getFriends() {
            return friends;
        }

        static class Friend {
            private String address;
            private String displayName;

            public String getAddress() {
                return address;
            }

            public String getDisplayName() {
                return displayName;
            }
        }
    }


    private static class Message {
        private static final AtomicInteger lastId = new AtomicInteger(0);
        private int id;
        private String type;

        private Message(String type) {
            this.id = lastId.getAndIncrement();
            this.type = type;
        }

        private Message() {} // to be used for response

        public int getId() {
            return id;
        }

        public String getType() {
            return type;
        }
    }

    private static class ResponseInfo<T extends Response> {
        private final Class<T> clazz;
        private final Function<T, Void> callback;

        private ResponseInfo(Class<T> clazz, Function<T, Void> callback) {
            this.clazz = clazz;
            this.callback = callback;
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public Function<T, Void> getCallback() {
            return callback;
        }
    }
}
