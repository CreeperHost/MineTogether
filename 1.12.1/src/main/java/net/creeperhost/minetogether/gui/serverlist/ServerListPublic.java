package net.creeperhost.minetogether.gui.serverlist;

import com.google.common.collect.Lists;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.creeperhost.minetogether.gui.serverlist.GuiMultiplayerPublic.*;
import static net.creeperhost.minetogether.gui.serverlist.GuiMultiplayerPublic.SortOrder.*;

public class ServerListPublic extends ServerList
{
    private final GuiMultiplayerPublic owner;
    private List<ServerData> servers;

    public ServerListPublic(Minecraft mcIn, GuiMultiplayerPublic owner)
    {
      super(mcIn);
      this.owner = owner;
    }

    @Override
    public void loadServerList()
    {
        if (owner == null)
            return; // to handle the super constructor calling us before we're ready.
        if (servers == null) servers = Lists.newArrayList();
        servers.clear();
        List<Server> list = Callbacks.getServerList(owner.isPublic);

        switch (owner.sortOrder)
        {
            default:
            case RANDOM:
                Collections.shuffle(list);
                break;
            case PLAYER:
                Collections.sort(list, new Server.PlayerComparator());
                break;
            case UPTIME:
                Collections.sort(list, new Server.UptimeComparator());
                break;
            case NAME:
                Collections.sort(list, new Server.NameComparator());
                break;
        }

        for(Server server: list)
        {
            servers.add(new ServerData(server.displayName, server.host, false));
        }
    }

    @Override
    public ServerData getServerData(int index)
    {
        return this.servers.get(index);
    }

    @Override
    public void removeServerData(int index)
    {
        this.servers.remove(index);
    }

    @Override
    public void addServerData(ServerData server)
    {
        this.servers.add(server);
    }

    @Override
    public int countServers()
    {
        return this.servers.size();
    }

    @Override
    public void swapServers(int pos1, int pos2)
    {
    }

    @Override
    public void set(int index, ServerData server)
    {
    }

    @Override
    public void saveServerList()
    {
    }
}
