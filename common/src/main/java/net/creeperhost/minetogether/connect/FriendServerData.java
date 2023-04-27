package net.creeperhost.minetogether.connect;

import net.minecraft.client.multiplayer.ServerData;

/**
 * Created by brandon3055 on 27/04/2023
 */
public class FriendServerData extends ServerData {

    public RemoteServer server;

    public FriendServerData(RemoteServer server) {
        super("", "", false);
    }
}
