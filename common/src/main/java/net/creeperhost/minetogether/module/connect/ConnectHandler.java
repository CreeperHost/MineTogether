package net.creeperhost.minetogether.module.connect;

import com.google.gson.Gson;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogetherconnect.ConnectMain;
import net.creeperhost.minetogetherconnect.ConnectUtil;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConnectHandler
{
    private static final HashMap<Integer, ResponseInfo> awaiting = new HashMap<>();
    private static final Object lock = new Object();
    private static final Gson gson = new Gson();
    private static Socket socket = null;

    public static void connectToProc()
    {
        ConnectHelper.isEnabled = ConnectMain.doAuth();
        if (!ConnectHelper.isEnabled) {
            MineTogether.logger.info("MineTogether Connect not enabled: " + ConnectMain.authError);
        }
    }

    public static boolean sendMessage(Message message, Function<Response, Void> callback)
    {
        return true;
        //return sendMessage(message, new ResponseInfo(Response.class, callback));
    }

    public static void openCallback(Consumer<String> messageRelayer, Consumer<Response> responseConsumer) {
        ConnectMain.listen((success, message) -> {
            Response response = new Response();
            response.success = success;
            response.message = message;
            responseConsumer.accept(response);
        }, messageRelayer);
    }

    public static <E extends Response> E blocking(Function<Function<E, Void>, Void> func)
    {

        AtomicReference<E> tempResponse = new AtomicReference<>();
        final Object tempLock = new Object();
        func.apply((response) ->
        {
            tempResponse.set(response);
            synchronized (tempLock)
            {
                tempLock.notifyAll();
            }

            return null;
        });

        try
        {
            synchronized (tempLock)
            {
                tempLock.wait();
            }
        } catch (InterruptedException ignored)
        {
        }

        return tempResponse.get();
    }

    public static void close()
    {
        ConnectMain.close();
    }

    public static FriendsResponse getFriendsBlocking()
    {
        return ConnectMain.getBackendServer().getFriends();
    }

    static class Response extends Message
    {
        private boolean success;
        private String message;

        public boolean isSuccess()
        {
            return success;
        }

        public String getMessage()
        {
            return message;
        }
    }

    public static class FriendsResponse extends Response
    {
        private ArrayList<Friend> friends;
        public ArrayList<Friend> getFriends()
        {
            return friends;
        }

        static class Friend
        {
            private String hash;
            private String displayName;
            private int port;

            public String getHash()
            {
                return hash;
            }

            public String getDisplayName()
            {
                return displayName;
            }

            public int getPort()
            {
                return port;
            }
        }
    }


    private static class Message
    {
        private static final AtomicInteger lastId = new AtomicInteger(0);
        private int id;
        private String type;

        private Message(String type)
        {
            this.id = lastId.getAndIncrement();
            this.type = type;
        }

        private Message()
        {
        } // to be used for response

        public int getId()
        {
            return id;
        }

        public String getType()
        {
            return type;
        }
    }

    private static class ResponseInfo<T extends Response>
    {
        private final Class<T> clazz;
        private final Function<T, Void> callback;

        private ResponseInfo(Class<T> clazz, Function<T, Void> callback)
        {
            this.clazz = clazz;
            this.callback = callback;
        }

        public Class<T> getClazz()
        {
            return clazz;
        }

        public Function<T, Void> getCallback()
        {
            return callback;
        }
    }
}
