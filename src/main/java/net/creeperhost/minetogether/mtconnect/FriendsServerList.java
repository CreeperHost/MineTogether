package net.creeperhost.minetogether.mtconnect;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.data.Profile;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.resources.I18n;
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
    private GuiMultiplayer owner;

    private List<ServerData> pendingFriendServers = new ArrayList<>();

    public FriendsServerList(LanServerDetector.LanServerList wrapped, GuiMultiplayer owner) {
        this.owner = owner;
        this.wrapped = wrapped;
        oursWasUpdated = true;
        addOurServer("127.0.0.1:42069", "MTConnect Test");

        CompletableFuture.runAsync(() ->
        {
            ArrayList<Friend> friendsList = Callbacks.getFriendsList(false);
            while(friendsList == null) {
                friendsList = Callbacks.getFriendsList(false);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }


            for(Friend friend : friendsList)
            {
                Profile profile = friend.getProfile();
                String ipv6 = "2a04:de41:" + String.join(":", profile.longHash.substring(0,24).split("(?<=\\G....)"));
                ServerData server = new ServerData(friend.getName() + "'s server", "[" + ipv6 + "]:42069", false);
                try
                {
                    CreeperHost.logger.info("Pinging server " + server);
                    this.owner.getOldServerPinger().ping(server);
                }
                catch (UnknownHostException var2)
                {
                    CreeperHost.logger.info("Can't resolve " + server);
                    server.pingToServer = -1L;
                    server.serverMOTD = TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_resolve");
                }
                catch (Exception var3)
                {
                    CreeperHost.logger.info("Can't connect " + server);
                    server.pingToServer = -1L;
                    server.serverMOTD = TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_connect");
                }
                if(server.pingToServer > 0)
                {
                    addPendingServer(server);
                }
            }
        });

    }

    public synchronized void addOurServer(String address, String friendName) {
        LanServerInfo lanServerInfo = new LanServerInfo(friendName + "'s MTConnect Server", address);
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
                    System.out.println("Removing " + friendServer + " " + friendServer.serverMOTD);
                    removingServers.add(friendServer);
                    if(!friendServer.serverMOTD.startsWith(TextFormatting.DARK_RED.toString()))
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
