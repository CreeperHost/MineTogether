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
        Map <String, String> map = Callbacks.getServerList(owner.isPublic);

        List<Map.Entry<String,String>> list = new ArrayList<Map.Entry<String,String>>(map.entrySet());

        Collections.shuffle(list);

        for(Map.Entry<String, String> server: list)
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
    public void set(int index, ServerData server)
    {
    }

    @Override
    public void saveServerList()
    {
    }
}
