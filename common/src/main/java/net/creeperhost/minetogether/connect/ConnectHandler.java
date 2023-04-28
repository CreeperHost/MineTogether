package net.creeperhost.minetogether.connect;

import com.mojang.logging.LogUtils;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.connect.gui.FriendConnectScreen;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.connect.web.FriendServerListRequest;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.web.ApiClient;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.lib.web.WebUtils;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.client.multiplayer.resolver.ServerRedirectHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by brandon3055 on 21/04/2023
 */
public class ConnectHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<RemoteServer, Profile> AVAILABLE_SERVER_MAP = new HashMap<>();

    private static long lastSearch = 0;
    private static CompletableFuture<?> activeSearch = null;
    private static FriendServerListRequest.Response searchResult = null;

    public static void init() {
    }

    public static boolean isEnabled() {
        return true; //TODO v2
    }

    public static String getProxyAddress() {
        return "localhost";
    }

    public static int getProxyPort() {
        return 32437;
    }

    public static int getHTTPPort() {
        return 32436;
    }

    public static void updateFriendsSearch() {
        if (activeSearch != null) {
            if (!activeSearch.isDone()) return;

            activeSearch = null;

            if (searchResult != null) {
                AVAILABLE_SERVER_MAP.clear();
                ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;

                for (FriendServerListRequest.Response.ServerEntry entry : searchResult.servers) {
                    RemoteServer server = new RemoteServer(entry.friend(), entry.serverToken());
                    Profile profile = profileManager.lookupProfile(entry.friend());
                    AVAILABLE_SERVER_MAP.put(server, profile);
                }

                searchResult = null;
            }
            return;
        }

        if (System.currentTimeMillis() - lastSearch < 5000) {
            return;
        }
        lastSearch = System.currentTimeMillis();

        activeSearch = CompletableFuture.runAsync(() -> {
            searchResult = null;
            try {
                JWebToken token = MineTogetherClient.getSession().get().orThrow();

                ApiClientResponse<FriendServerListRequest.Response> res = MineTogether.API.execute(new FriendServerListRequest("http://" + getProxyAddress() + ":" + getHTTPPort(), token.toString()));
                if (res.statusCode() != 200) {
                    LOGGER.error("An error occurred while searching for friend servers, Response code: {}, Message: {}", res.statusCode(), res.message());
                    return;
                }
                searchResult = res.apiResponse();
            } catch (Throwable e) {
                LOGGER.error("An error occurred while searching for friend servers.", e);
            }
        });
    }

    public static Collection<RemoteServer> getRemoteServers() {
        return AVAILABLE_SERVER_MAP.keySet();
    }

    public static Profile getServerProfile(RemoteServer server) {
        return AVAILABLE_SERVER_MAP.get(server);
    }

    public static void clearRemotes() {
        AVAILABLE_SERVER_MAP.clear();
    }
}
