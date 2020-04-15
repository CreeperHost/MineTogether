package net.creeperhost.minetogether.client.gui.serverlist.gui.elements;

import com.google.common.collect.Lists;
import net.creeperhost.minetogether.client.gui.serverlist.data.Server;
import net.creeperhost.minetogether.client.gui.serverlist.data.ServerDataPublic;
import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiMultiplayerPublic;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

import java.util.List;

public class ServerListPublic extends ServerList
{
    private final GuiMultiplayerPublic owner;
    public List<ServerDataPublic> servers;
    
    public ServerListPublic(Minecraft mcIn, GuiMultiplayerPublic owner)
    {
        super(mcIn);
        this.owner = owner;
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void loadServerList()
    {
        if (owner == null)
            return; // to handle the super constructor calling us before we're ready.
        if (servers == null) servers = Lists.newArrayList();
        servers.clear();
        List<Server> list = Callbacks.getServerList(owner.listType);
        
        for (Server server : list)
        {
            servers.add(new ServerDataPublic(server));
        }
    }
    
    @Override
    public ServerDataPublic getServerData(int index)
    {
        return this.servers.get(index);
    }
    
    @Override
    public void func_217506_a(ServerData index)
    {
        this.servers.remove(index);
    }
    
    @Override
    public void addServerData(ServerData server)
    {
        this.servers.add((ServerDataPublic) server);
    }
    
    @Override
    public int countServers()
    {
        return this.servers.size();
    }
    
    @Override
    public void swapServers(int pos1, int pos2)
    {
        super.swapServers(pos1, pos2);
    }
    
    @Override
    public void set(int index, ServerData server)
    {
        super.set(index, server);
    }
    
    @Override
    public void saveServerList()
    {
        super.saveServerList();
    }
}
