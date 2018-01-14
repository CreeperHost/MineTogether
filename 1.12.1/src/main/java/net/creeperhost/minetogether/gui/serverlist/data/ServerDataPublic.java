package net.creeperhost.minetogether.gui.serverlist.data;

import net.minecraft.client.multiplayer.ServerData;

public class ServerDataPublic extends ServerData
{
    public Server server;

    public ServerDataPublic(Server server)
    {
        super(server.displayName, server.host, false);
        this.server = server;
    }
}
