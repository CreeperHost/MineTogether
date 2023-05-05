package net.creeperhost.minetogether.connect;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.connect.lib.util.RSAUtils;
import net.creeperhost.minetogether.connect.lib.web.GetConnectServersRequest;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.connect.web.FriendServerListRequest;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by brandon3055 on 21/04/2023
 */
public class ConnectHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<RemoteServer, Profile> AVAILABLE_SERVER_MAP = new HashMap<>();
    private static final ExecutorService SEARCH_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("MT Connect Friend Search Executor").build());
    private static final ExecutorService SHARE_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("MT Connect Friend Share Executor").build());

    private static long lastSearch = 0;
    private static CompletableFuture<?> activeSearch = null;
    private static FriendServerListRequest.Response searchResult = null;

    @Nullable
    private static ConnectHost endpoint;

    public static void init() {
    }

    public static ConnectHost getEndpoint() {
        if (endpoint == null) {
            GetConnectServersRequest.ConnectServer node = chooseServer();
            LOGGER.info("Selected MTConnect server: " + node.name);

            endpoint = new ConnectHost(
                    node.ssl ? "https" : "http",
                    node.address,
                    node.port,
                    node.port + 1,
                    RSAUtils.loadRSAPublicKey(RSAUtils.loadPem(node.publicKey))
            );
        }
        return endpoint;
    }

    private static GetConnectServersRequest.ConnectServer chooseServer() {
        if (Boolean.getBoolean("mt.develop.connect")) {
            return GetConnectServersRequest.ConnectServer.getLocalHost();
        }
        try {
            List<GetConnectServersRequest.ConnectServer> servers = MineTogether.API.execute(new GetConnectServersRequest()).apiResponse();
            if (servers.isEmpty()) {
                // TODO, this needs to gracefully fail as noted bellow.
                LOGGER.warn("No MTConnect nodes found.. :(");
                throw new NotImplementedException();
            }

            // TODO this needs to get geography data and select the closest node.
            return servers.get(0);
        } catch (IOException ex) {
            // TODO, this needs to gracefully fail, getEndpoint likely needs to return null, and isEnabled needs to return false.
            throw new NotImplementedException("TODO, Implement exception handling for this:", ex);
        }
    }

    public static boolean isEnabled() {
        return true; //TODO v2
    }

    public static void publishToFriends(GameType gameType, boolean cheats) {
        // Mostly copy of IntegratedServer#publishServer
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) return;
        mc.prepareForMultiplayer();

        server.publishedPort = 0; // Doesn't matter, just set to _something_.
        server.publishedGameType = gameType;
        server.getPlayerList().setAllowCheatsForAllPlayers(cheats);
        mc.player.setPermissionLevel(server.getProfilePermissions(mc.player.getGameProfile()));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }

        CompletableFuture.runAsync(() -> {
            try { // TODO, This should be done outside somewhere.
                JWebToken token = MineTogetherClient.getSession().get().orThrow();
                NettyClient.publishServer(server, getEndpoint(), token);
            } catch (Exception e) {
                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("minetogether.connect.open.failed"));
                LOGGER.error("Failed to open to friends", e);
                unPublish();
            }
        }, SHARE_EXECUTOR);
    }

    public static void unPublish() {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) return;
        //Un-Share the world.
        server.publishedPort = -1;
        server.publishedGameType = null;
    }

    public static void updateFriendsSearch() {
        if (activeSearch != null) {
            if (!activeSearch.isDone()) return;

            activeSearch = null;

            if (searchResult != null) {
                ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
                Set<RemoteServer> keep = new HashSet<>();
                for (FriendServerListRequest.Response.ServerEntry entry : searchResult.servers) {
                    RemoteServer server = new RemoteServer(entry.friend(), entry.serverToken());
                    keep.add(server);
                    if (!AVAILABLE_SERVER_MAP.containsKey(server)) {
                        Profile profile = profileManager.lookupProfile(entry.friend());
                        AVAILABLE_SERVER_MAP.put(server, profile);
                    }
                }

                AVAILABLE_SERVER_MAP.entrySet().removeIf(entry -> !keep.contains(entry.getKey()));
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
        }, SEARCH_EXECUTOR);
    }

    public static Collection<RemoteServer> getRemoteServers() {
        return AVAILABLE_SERVER_MAP.keySet();
    }

    public static Profile getServerProfile(RemoteServer server) {
        return AVAILABLE_SERVER_MAP.get(server);
    }

    public static void clearAndReset() {
        if (activeSearch != null) {
            activeSearch.cancel(true);
            activeSearch = null;
            searchResult = null;
        }
        AVAILABLE_SERVER_MAP.clear();
        lastSearch = 0;
    }
}
