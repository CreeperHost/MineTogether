package net.creeperhost.minetogether.gui.serverlist;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerListPublic extends ServerList
{
    private final GuiMultiplayerPublic owner;
    private List<ServerDataPublic> servers;

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

        for(Server server: list)
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
    public void removeServerData(int index)
    {
        this.servers.remove(index);
    }

    @Override
    public void addServerData(ServerData server)
    {
        this.servers.add((ServerDataPublic)server);
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
