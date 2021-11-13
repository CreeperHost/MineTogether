package net.creeperhost.minetogether.module.connect;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FriendsServerList extends LanServerDetection.LanServerList
{
    private final LanServerDetection.LanServerList wrapped;
    public static ScheduledExecutorService detectorExecutor;
    private boolean oursWasUpdated;
    private final List<LanServer> ourLanServers = new ArrayList<>();
    final JoinMultiplayerScreen owner;

    FriendDetector detector;

    private final List<FriendDetector.PendingFriend> pendingFriendServers = new ArrayList<>();

    public FriendsServerList(JoinMultiplayerScreen owner, LanServerDetection.LanServerList wrapped)
    {
        this.wrapped = wrapped;
        this.owner = owner;
        oursWasUpdated = true;
        if (ConnectHelper.isEnabled)
        {
            detectorExecutor = Executors.newSingleThreadScheduledExecutor();
            detectorExecutor.scheduleAtFixedRate(new FriendDetector(this), 0, 5, TimeUnit.SECONDS);
        }
    }

    public synchronized void addOurServer(FriendDetector.PendingFriend friend)
    {
        LanServerInfoConnect lanServerInfo = new LanServerInfoConnect(friend);
        //TODO: Use friend face with MT logo in corner
        ourLanServers.add(lanServerInfo);
        oursWasUpdated = true;
    }

    public synchronized void addPendingServer(FriendDetector.PendingFriend data)
    {
        synchronized (pendingFriendServers)
        {
            for (FriendDetector.PendingFriend server : pendingFriendServers)
            {
                if (data.getAddress().equals(server.getAddress()))
                {
                    return;
                }
            }
            synchronized (ourLanServers)
            {
                for (LanServer server : ourLanServers)
                {
                    if (data.getAddress().equals(server.getAddress()))
                    {
                        return;
                    }
                }
            }
            pendingFriendServers.add(data);
        }
    }

    private long lastCheckTime = System.currentTimeMillis() - 1000;

    @Override
    public synchronized boolean isDirty()
    {
        long curTime = System.currentTimeMillis();
        if (lastCheckTime + 1000 <= curTime)
        {
            ArrayList<FriendDetector.PendingFriend> removingServers = new ArrayList<>();
            synchronized (pendingFriendServers)
            {
                for (FriendDetector.PendingFriend friendServer : pendingFriendServers)
                {
                    removingServers.add(friendServer);
                    addOurServer(friendServer);
                    oursWasUpdated = true;
                }
                pendingFriendServers.removeAll(removingServers);
            }

            lastCheckTime = System.currentTimeMillis();
        }
        return oursWasUpdated || wrapped.isDirty();
    }

    @Override
    public synchronized void addServer(String pingResponse, InetAddress ipAddress)
    {
        wrapped.addServer(pingResponse, ipAddress);
    }

    @Override
    public synchronized void markClean()
    {
        wrapped.markClean();
        oursWasUpdated = false;
    }

    @Override
    public synchronized List<LanServer> getServers()
    {
        return Collections.unmodifiableList(Lists.newArrayList(Iterables.concat(wrapped.getServers(), ourLanServers))); // maybe not the most efficient, but not called that much
    }

    public void removed()
    {
        if (detectorExecutor != null)
        {
            detectorExecutor.shutdown();
        }
    }
}
