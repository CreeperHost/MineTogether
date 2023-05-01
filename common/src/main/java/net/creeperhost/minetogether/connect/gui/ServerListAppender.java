package net.creeperhost.minetogether.connect.gui;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.ConnectHost;
import net.creeperhost.minetogether.connect.RemoteServer;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.mixin.connect.ServerSelectionListAccessor;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by brandon3055 on 21/04/2023
 */
public class ServerListAppender {
    //Had to use a static instance here because I could not find another way to share a wrapper instance between JoinMultiplayerScreenMixin and ServerSelectionListMixin
    public static final ServerListAppender INSTANCE = new ServerListAppender();

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component CANT_CONNECT_MESSAGE = Component.translatable("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);

    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    private ServerSelectionList serverList;
    private JoinMultiplayerScreen multiplayerScreen;

    private Map<RemoteServer, FriendServerEntry> serverEntries = new HashMap<>();
    private int tick = 0;

    public void init(ServerSelectionList serverList, JoinMultiplayerScreen multiplayerScreen) {
        this.serverList = serverList;
        this.multiplayerScreen = multiplayerScreen;
        ConnectHandler.clearAndReset();
        ConnectHandler.updateFriendsSearch();
    }

    public void tick() {
        if (tick++ % 20 != 0 || serverList == null || multiplayerScreen == null) return;
        ConnectHandler.updateFriendsSearch();

        boolean dirty = false;
        List<RemoteServer> remoteServers = new ArrayList<>(ConnectHandler.getRemoteServers());

        //Add new servers
        for (RemoteServer remoteServer : remoteServers) {
            if (!serverEntries.containsKey(remoteServer)) {
                serverEntries.put(remoteServer, new FriendServerEntry(multiplayerScreen, remoteServer, this));
                dirty = true;
            }
        }

        //Remove servers that are no longer available
        List<RemoteServer> toRemove = new ArrayList<>();
        serverEntries.forEach((server, entry) -> {
            if (!remoteServers.contains(server)) {
                toRemove.add(server);
            }
        });

        if (!toRemove.isEmpty()) {
            toRemove.forEach(serverEntries::remove);
            dirty = true;
        }

        if (dirty) {
            ((ServerSelectionListAccessor) serverList).invokeRefreshEntries();
        }

        synchronized (this.connections) {
            Iterator<Connection> iterator = this.connections.iterator();

            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (connection.isConnected()) {
                    connection.tick();
                } else {
                    iterator.remove();
                    connection.handleDisconnection();
                }
            }
        }
    }

    public void remove() {
        serverList = null;
        multiplayerScreen = null;
        serverEntries.clear();
        removeAll();
    }

    //Add entries to server list, Called from the end of ServerSelectionList#refreshEntries
    public void addEntries() {
        if (serverList == null) return;
//        serverList.addEntry(new FriendsHeader());
        for (FriendServerEntry entry : serverEntries.values()) {
            serverList.addEntry(entry);
        }
    }

    public void pingServer(RemoteServer server, Profile profile) throws ExecutionException, InterruptedException {
        JWebToken token = MineTogetherClient.getSession().get().orThrow();
        Connection connection = NettyClient.connect(ConnectHandler.getEndpoint(), token, server.serverToken());
        connections.add(connection);
        server.motd = Component.translatable("multiplayer.status.pinging");
        server.ping = -1L;
        server.playerList = null;

        connection.setListener(new ClientStatusPacketListener() {
            private boolean success;
            private boolean receivedPing;
            private long pingStart;

            public void handleStatusResponse(ClientboundStatusResponsePacket clientboundStatusResponsePacket) {
                if (this.receivedPing) {
                    connection.disconnect(Component.translatable("multiplayer.status.unrequested"));
                } else {
                    this.receivedPing = true;
                    ServerStatus serverStatus = clientboundStatusResponsePacket.getStatus();
                    if (serverStatus.getDescription() != null) {
                        server.motd = serverStatus.getDescription();
                    } else {
                        server.motd = CommonComponents.EMPTY;
                    }

                    if (serverStatus.getVersion() != null) {
                        server.version = Component.literal(serverStatus.getVersion().getName());
                        server.protocol = serverStatus.getVersion().getProtocol();
                    } else {
                        server.version = Component.translatable("multiplayer.status.old");
                        server.protocol = 0;
                    }

                    if (serverStatus.getPlayers() != null) {
                        server.status = formatPlayerCount(serverStatus.getPlayers().getNumPlayers(), serverStatus.getPlayers().getMaxPlayers());
                        List<Component> list = Lists.newArrayList();
                        GameProfile[] gameProfiles = serverStatus.getPlayers().getSample();
                        if (gameProfiles != null && gameProfiles.length > 0) {
                            GameProfile[] var5 = gameProfiles;
                            int var6 = gameProfiles.length;

                            for (int var7 = 0; var7 < var6; ++var7) {
                                GameProfile gameProfile = var5[var7];
                                list.add(Component.literal(gameProfile.getName()));
                            }

                            if (gameProfiles.length < serverStatus.getPlayers().getNumPlayers()) {
                                list.add(Component.translatable("multiplayer.status.and_more", serverStatus.getPlayers().getNumPlayers() - gameProfiles.length));
                            }

                            server.playerList = list;
                        }
                    } else {
                        server.status = Component.translatable("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY);
                    }

                    String string = serverStatus.getFavicon();
                    if (string != null) {
                        try {
                            string = ServerData.parseFavicon(string);
                        } catch (ParseException var9) {
                            LOGGER.error("Invalid server icon", var9);
                        }
                    }

                    if (!Objects.equals(string, server.getIconB64())) {
                        server.setIconB64(string);
                    }

                    this.pingStart = Util.getMillis();
                    connection.send(new ServerboundPingRequestPacket(this.pingStart));
                    this.success = true;
                }
            }

            public void handlePongResponse(ClientboundPongResponsePacket clientboundPongResponsePacket) {
                long l = this.pingStart;
                long m = Util.getMillis();
                server.ping = m - l;
                connection.disconnect(Component.translatable("multiplayer.status.finished"));
            }

            public void onDisconnect(Component component) {
                if (!this.success) {
                    onPingFailed(component, server, profile);
//                    pingLegacyServer(inetSocketAddress, server);
                }
            }

            public Connection getConnection() {
                return connection;
            }
        });

        try {
            ConnectHost endpoint = ConnectHandler.getEndpoint();
            connection.send(new ClientIntentionPacket(endpoint.host(), endpoint.proxyPort(), ConnectionProtocol.STATUS));
            connection.send(new ServerboundStatusRequestPacket());
        } catch (Throwable var8) {
            LOGGER.error( "Failed to ping friend server {}", server.friend(), var8);
        }
    }

    private static Component formatPlayerCount(int i, int j) {
        return Component.literal(Integer.toString(i)).append(Component.literal("/").withStyle(ChatFormatting.DARK_GRAY)).append(Integer.toString(j)).withStyle(ChatFormatting.GRAY);
    }

    private void onPingFailed(Component component, RemoteServer server, Profile profile) {
        LOGGER.error("Can't ping {}: {}", profile.isFriend() ? profile.getFriendName() : profile.getDisplayName(), component.getString());
        server.motd = CANT_CONNECT_MESSAGE;
        server.status = CommonComponents.EMPTY;
    }

    public void removeAll() {
        synchronized (this.connections) {
            Iterator<Connection> iterator = this.connections.iterator();

            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (connection.isConnected()) {
                    iterator.remove();
                    connection.disconnect(Component.translatable("multiplayer.status.cancelled"));
                }
            }
        }
    }

    @Nullable
    public ServerSelectionList getServerList() {
        return serverList;
    }
}


























