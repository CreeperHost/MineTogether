package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.connect.netty.packet.SHostRegister;
import net.creeperhost.minetogether.session.JWebToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.concurrent.ExecutionException;

/**
 * Created by covers1624 on 24/4/23.
 */
public class ShareHandler {

    public static void publishToFriends(GameType gameType, boolean cheats) {
        // Mostly copy of IntegratedServer#publishServer
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        mc.prepareForMultiplayer();
        JWebToken token;
        try { // TODO, This should be done outside somewhere.
            token = MineTogetherClient.getSession().get().orThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        NettyClient.publishServer(server, "localhost", 32437, token);
        server.publishedPort = 0; // Doesn't matter, just set to _something_.
        server.publishedGameType = gameType;
        server.getPlayerList().setAllowCheatsForAllPlayers(cheats);
        mc.player.setPermissionLevel(server.getProfilePermissions(mc.player.getGameProfile()));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
    }
}
