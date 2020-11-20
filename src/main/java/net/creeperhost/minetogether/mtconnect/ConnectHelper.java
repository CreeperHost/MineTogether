package net.creeperhost.minetogether.mtconnect;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class ConnectHelper {

    static boolean isEnabled = false;

    public static boolean shareToFriends(GameType type, boolean allowCheats) {
        IntegratedServer integratedServer = Minecraft.getMinecraft().getIntegratedServer();
        try
        {
            int port = 42069;

            System.setProperty("java.net.preferIPv4Stack", "false"); // no tears, not only ipv4
            integratedServer.getNetworkSystem().addLanEndpoint(null, port);
            ObfuscationReflectionHelper.setPrivateValue(IntegratedServer.class, integratedServer, true, "isPublic", "field_71346_p");
            integratedServer.getPlayerList().setGameType(type);
            integratedServer.getPlayerList().setCommandsAllowedForAll(allowCheats);
            Minecraft.getMinecraft().player.setPermissionLevel(allowCheats ? 4 : 0);
            return true;
        }
        catch (IOException var6)
        {
            return false;
        }
    }

}
