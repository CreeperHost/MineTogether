package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.IOException;

public class ConnectHelper {

    public static boolean isEnabled = false;

    public static boolean shareToFriends(GameType type, boolean allowCheats) {
        IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
        try
        {
            int port = 42069;
            System.setProperty("java.net.preferIPv4Stack", "false"); // no tears, not only ipv4
            integratedServer.getNetworkSystem().addEndpoint(null, port);
            integratedServer.setServerPort(port);
            integratedServer.getPlayerList().setGameType(type);
            integratedServer.getPlayerList().setCommandsAllowedForAll(allowCheats);
            Minecraft.getInstance().player.setPermissionLevel(allowCheats ? 4 : 0);
            return true;
        }
        catch (IOException var6)
        {
            return false;
        }
    }

}
