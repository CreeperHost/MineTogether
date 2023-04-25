package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.connect.netty.packet.SHostRegister;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;

import java.util.concurrent.ExecutionException;

/**
 * Created by covers1624 on 24/4/23.
 */
public class ShareHandler {

    public static void publishToFriends(GameType gameMode, boolean commands) {
        // TODO mostly copy of IntegratedServer#publishServer
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        mc.prepareForMultiplayer();
        JWebToken token;
        try { // TODO, This should be done outside somewhere.
            token = MineTogetherClient.getSession().get().orThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        NettyClient client = new NettyClient(server, "localhost", 32437, () -> new SHostRegister(token.toString()));
        client.start();
    }
}
