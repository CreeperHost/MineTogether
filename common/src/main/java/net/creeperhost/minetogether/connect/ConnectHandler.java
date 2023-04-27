package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.connect.web.FriendServerListRequest;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    private static ConnectHost endpoint;

    public static void init() {
    }

    public static ConnectHost getEndpoint() {
        if (endpoint == null) {
            endpoint = ConnectHost.LOCALHOST; // TODO actually resolve closest endpoint.
        }
        return endpoint;
    }

    public static boolean isEnabled() {
        return true; //TODO v2
    }

    public static void publishToFriends(GameType gameType, boolean cheats) {
        // Mostly copy of IntegratedServer#publishServer
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        mc.prepareForMultiplayer();
        JWebToken token;
        try { // TODO, This should be done outside somewhere.
            token = MineTogetherClient.getSession().get().orThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        NettyClient.publishServer(server, getEndpoint(), token);
        server.publishedPort = 0; // Doesn't matter, just set to _something_.
        server.publishedGameType = gameType;
        server.getPlayerList().setAllowCheatsForAllPlayers(cheats);
        mc.player.setPermissionLevel(server.getProfilePermissions(mc.player.getGameProfile()));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
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

                ApiClientResponse<FriendServerListRequest.Response> res = MineTogether.API.execute(new FriendServerListRequest(getEndpoint().httpUrl(), token.toString()));
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
