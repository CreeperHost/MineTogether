package net.creeperhost.creeperhost.gui.serverlist;

import com.google.common.collect.Lists;
import net.creeperhost.creeperhost.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

import java.util.List;
import java.util.Map;

public class ServerListPublic extends ServerList
{
    private List<ServerData> servers;

    public ServerListPublic(Minecraft mcIn)
    {
      super(mcIn);
    }

    @Override
    public void loadServerList()
    {
        if (servers == null) servers = Lists.newArrayList();
        servers.clear();
        Map <String, String> map = Callbacks.getServerList();
        for(Map.Entry<String, String> server : map.entrySet())
        {
            servers.add(new ServerData(server.getValue(), server.getKey(), false));
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
    public void func_147413_a(int index, ServerData server)
    {
    }

    @Override
    public void saveServerList()
    {
    }
}
