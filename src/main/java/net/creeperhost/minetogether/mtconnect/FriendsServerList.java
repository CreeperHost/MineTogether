package net.creeperhost.minetogether.mtconnect;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendsServerList extends LanServerDetector.LanServerList {
    private final LanServerDetector.LanServerList wrapped;
    private boolean oursWasUpdated = false;
    private List<LanServerInfo> ourLanServers = new ArrayList<>();
    private MultiplayerScreen owner;

    private List<ServerData> pendingFriendServers = new ArrayList<>();

    public FriendsServerList(LanServerDetector.LanServerList wrapped, MultiplayerScreen owner) {
        this.owner = owner;
        this.wrapped = wrapped;
        oursWasUpdated = true;
        if(ConnectHelper.isEnabled) {
            CompletableFuture.runAsync(() ->
            {
                ArrayList<Friend> friendsList = Callbacks.getFriendsList(false);
                while (friendsList == null) {
                    friendsList = Callbacks.getFriendsList(false);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                for (Friend friend : friendsList) {
                    CompletableFuture.runAsync(() -> {
                        Profile profile = friend.getProfile();
                        if (!profile.isOnline()) return;
                        ServerData server = new ServerData(friend.getName() + "'s server", "[" + profile.getConnectAddress() + "]:42069", false);
                        try {
                            this.owner.getOldServerPinger().ping(server, null);
                        } catch (UnknownHostException var2) {
                            server.pingToServer = -1L;
                            server.serverMOTD = new StringTextComponent(TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_resolve"));
                        } catch (Exception var3) {
                            server.pingToServer = -1L;
                            server.serverMOTD = new StringTextComponent(TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_connect"));
                        }
                        if (server.pingToServer > 0) {
                            addPendingServer(server);
                        }
                    }, MineTogether.otherExecutor);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
            });
        }
    }

    public synchronized void addOurServer(String address, String friendName) {
        LanServerInfo lanServerInfo = new LanServerInfo(friendName + "'s world", address);
        //TODO: Use MineTogether connect logo as icon for server?
        ourLanServers.add(lanServerInfo);
        oursWasUpdated = true;
    }

    public synchronized void addPendingServer(ServerData data) {
        pendingFriendServers.add(data);
    }

    private long lastCheckTime = System.currentTimeMillis() - 1000;

    @Override
    public synchronized boolean getWasUpdated() {
        long curTime = System.currentTimeMillis();
        if (lastCheckTime + 1000 <= curTime)
        {
            ArrayList<ServerData> removingServers = new ArrayList<>();
            for(ServerData friendServer : pendingFriendServers)
            {
                if (!friendServer.serverMOTD.equals(I18n.format("multiplayer.status.pinging")))
                {
                    removingServers.add(friendServer);
                    if(!friendServer.serverMOTD.getString().startsWith(TextFormatting.DARK_RED.toString()))
                    {
                        addOurServer(friendServer.serverIP, friendServer.serverName);
                        oursWasUpdated = true;
                    }
                }
            }
            pendingFriendServers.removeAll(removingServers);
            lastCheckTime = System.currentTimeMillis();
        }
        return oursWasUpdated || wrapped.getWasUpdated();
    }

    @Override
    public synchronized void addServer(String pingResponse, InetAddress ipAddress) {
        wrapped.addServer(pingResponse, ipAddress);
    }

    @Override
    public synchronized void setWasNotUpdated() {
        wrapped.setWasNotUpdated();
        oursWasUpdated = false;
    }

    @Override
    public synchronized List<LanServerInfo> getLanServers() {
        return Collections.unmodifiableList(Lists.newArrayList(Iterables.concat(wrapped.getLanServers(), ourLanServers))); // maybe not the most efficient, but not called that much
    }
}
