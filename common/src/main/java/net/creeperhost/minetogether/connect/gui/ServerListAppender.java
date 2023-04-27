package net.creeperhost.minetogether.connect.gui;

import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.RemoteServer;
import net.creeperhost.minetogether.mixin.connect.ServerSelectionListAccessor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.*;

/**
 * Created by brandon3055 on 21/04/2023
 */
public class ServerListAppender {
    //Had to use a static instance here because I could not find another way to share a wrapper instance between JoinMultiplayerScreenMixin and ServerSelectionListMixin
    public static final ServerListAppender INSTANCE = new ServerListAppender();

    private ServerSelectionList serverList;
    private JoinMultiplayerScreen multiplayerScreen;

    private Map<RemoteServer, FriendServerEntry> serverEntries = new HashMap<>();

    private int tick = 0;

    public void init(ServerSelectionList serverList, JoinMultiplayerScreen multiplayerScreen) {
        this.serverList = serverList;
        this.multiplayerScreen = multiplayerScreen;
    }

    public void tick() {
        if (tick++ % 20 != 0 || serverList == null || multiplayerScreen == null) return;
        ConnectHandler.updateFriendsSearch();

        boolean dirty = false;
        List<RemoteServer> remoteServers = new ArrayList<>(ConnectHandler.getRemoteServers());

        //Add new servers
        for (RemoteServer remoteServer : remoteServers) {
            if (!serverEntries.containsKey(remoteServer)) {
                serverEntries.put(remoteServer, new FriendServerEntry(multiplayerScreen, remoteServer));
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
    }

    public void remove() {
        serverList = null;
        multiplayerScreen = null;
        //Do any required cleanup
    }

    //Add entries to server list, Called from the end of ServerSelectionList#refreshEntries
    public void addEntries() {
        if (serverList == null) return;
//        serverList.addEntry(new FriendsHeader());
        for (FriendServerEntry entry : serverEntries.values()) {
            serverList.addEntry(entry);
        }
    }

}
