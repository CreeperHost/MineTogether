package net.creeperhost.minetogether.serverlist.gui;

import net.creeperhost.minetogether.serverlist.data.Server;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ServerDataPublic extends ServerData {

    public final Server server;

    public ServerDataPublic(Server server) {
        super(server.name, server.ip + ":" + server.port, false);
        this.server = server;
    }
}
