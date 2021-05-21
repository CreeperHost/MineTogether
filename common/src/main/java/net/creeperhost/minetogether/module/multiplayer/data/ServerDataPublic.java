package net.creeperhost.minetogether.module.multiplayer.data;

import net.creeperhost.minetogetherlib.serverlists.Server;
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
