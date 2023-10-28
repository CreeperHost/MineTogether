package net.creeperhost.minetogether.serverlist.gui;

import net.creeperhost.minetogether.serverlist.data.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ServerDataPublic extends ServerData {

    public final Server server;
    private FaviconTexture icon;

    public ServerDataPublic(Server server) {
        super(server.name, server.ip + ":" + server.port, false);
        this.server = server;
    }

    public FaviconTexture getIcon() {
        if (icon == null) {
            icon = FaviconTexture.forServer(Minecraft.getInstance().getTextureManager(), ip);
        }
        return icon;
    }
}
