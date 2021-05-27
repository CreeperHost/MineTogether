package net.creeperhost.minetogether.mtconnect;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.LanServerInfo;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FriendsServerList extends LanServerDetector.LanServerList {
    private final LanServerDetector.LanServerList wrapped;
    public static ScheduledExecutorService detectorExecutor;
    private boolean oursWasUpdated;
    private final List<LanServerInfoConnect> ourLanServers = new ArrayList<>();

    final GuiMultiplayer owner;

    private final List<FriendDetector.PendingFriend> pendingFriendServers = new ArrayList<>();

    public FriendsServerList(GuiMultiplayer owner, LanServerDetector.LanServerList wrapped) {
        this.owner = owner;
        this.wrapped = wrapped;
        oursWasUpdated = true;
        if(ConnectHelper.isEnabled) {
            detectorExecutor = Executors.newSingleThreadScheduledExecutor();
            detectorExecutor.scheduleAtFixedRate(new FriendDetector(this), 0, 10, TimeUnit.SECONDS);
        }
    }

    public synchronized void addOurServer(FriendDetector.PendingFriend friend) {
        LanServerInfoConnect lanServerInfo = new LanServerInfoConnect(friend);
        //TODO: Use MineTogether connect logo as icon for server?
        synchronized (ourLanServers) {
            ourLanServers.add(lanServerInfo);
        }
    }

    public synchronized void addPendingServer(FriendDetector.PendingFriend data) {
        synchronized (pendingFriendServers) {
             for (FriendDetector.PendingFriend server: pendingFriendServers) {
                if(data.getAddress().equals(server.getAddress())) {
                    return;
                }
            }
            synchronized (ourLanServers) {
                for (LanServerInfo server: ourLanServers) {
                    if(data.getAddress().equals(server.getServerIpPort())) {
                        return;
                    }
                }
            }
            pendingFriendServers.add(data);
        }
    }

    private long lastCheckTime = System.currentTimeMillis() - 1000;

    @Override
    public synchronized boolean getWasUpdated() {
        long curTime = System.currentTimeMillis();
        if (lastCheckTime + 1000 <= curTime)
        {
            ArrayList<FriendDetector.PendingFriend> removingServers = new ArrayList<>();
            synchronized (pendingFriendServers) {
                for(FriendDetector.PendingFriend friendServer : pendingFriendServers)
                {
                    removingServers.add(friendServer);
                    addOurServer(friendServer);
                    oursWasUpdated = true;
                }
                pendingFriendServers.removeAll(removingServers);
            }

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
