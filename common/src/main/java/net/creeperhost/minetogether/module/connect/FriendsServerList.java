package net.creeperhost.minetogether.module.connect;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendsServerList extends LanServerDetection.LanServerList {
    private boolean oursWasUpdated;
    private final List<LanServer> ourLanServers = new ArrayList<>();
    final JoinMultiplayerScreen owner;
    FriendDetector detector;

    private final List<ServerData> pendingFriendServers = new ArrayList<>();

    public FriendsServerList(JoinMultiplayerScreen owner) {
        this.owner = owner;
        oursWasUpdated = true;
        if(ConnectHelper.isEnabled) {
            detector = new FriendDetector(this);
        }
    }

    public synchronized void addOurServer(String address, String friendName) {
        LanServer lanServerInfo = new LanServer(friendName + "'s world", address);
        //TODO: Use MineTogether connect logo as icon for server?
        ourLanServers.add(lanServerInfo);
        oursWasUpdated = true;
    }

    public synchronized void addPendingServer(ServerData data) {
        pendingFriendServers.add(data);
    }

    private long lastCheckTime = System.currentTimeMillis() - 1000;

    @Override
    public synchronized boolean isDirty() {
        long curTime = System.currentTimeMillis();
        if (lastCheckTime + 1000 <= curTime)
        {
            ArrayList<ServerData> removingServers = new ArrayList<>();
            for(ServerData friendServer : pendingFriendServers)
            {
                if (!friendServer.motd.getString().equals(I18n.get("multiplayer.status.pinging")))
                {
                    removingServers.add(friendServer);
                    if(!friendServer.motd.getString().startsWith(ChatFormatting.DARK_RED.toString()))
                    {
                        addOurServer(friendServer.ip, friendServer.name);
                    }
                }
            }
            pendingFriendServers.removeAll(removingServers);
            lastCheckTime = System.currentTimeMillis();
        }
        return oursWasUpdated;
    }

    @Override
    public synchronized void addServer(String pingResponse, InetAddress ipAddress) {
    }

    @Override
    public synchronized void markClean() {
        oursWasUpdated = false;
    }

    @Override
    public synchronized List<LanServer> getServers() {
        return Collections.unmodifiableList(ourLanServers); // maybe not the most efficient, but not called that much
    }

    public void removed() {
        if (detector != null) {
            detector.interrupt();
        }
    }
}
