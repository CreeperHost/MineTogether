package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ConnectHelper {

    public static boolean isEnabled = false;

    public static boolean isShared() {
        IntegratedServer integratedServer = Minecraft.getMinecraft().getIntegratedServer();
        return isShared(integratedServer);
    }

    public static boolean isShared(IntegratedServer integratedServer) {
        if(integratedServer == null) {
            return false;
        }
        return integratedServer.getServerPort() == 42069 && integratedServer.getPublic();
    }

    public static void shareToFriends(GameType type, boolean allowCheats) {
        CompletableFuture.runAsync(() -> {
            ConnectHandler.Response response = ConnectHandler.openBlocking();
            if (response.isSuccess()) {
                IntegratedServer integratedServer = Minecraft.getMinecraft().getIntegratedServer();
                Objects.requireNonNull(integratedServer).addScheduledTask(() -> {
                    try
                    {
                        int port = 42069;
                        integratedServer.getNetworkSystem().addLanEndpoint(null, port);
                        ObfuscationReflectionHelper.setPrivateValue(IntegratedServer.class, integratedServer, true, "isPublic", "field_71346_p");
                        integratedServer.getPlayerList().setGameType(type);
                        integratedServer.getPlayerList().setCommandsAllowedForAll(allowCheats);
                        Minecraft.getMinecraft().player.setPermissionLevel(allowCheats ? 4 : 0);
                    }
                    catch (IOException var6)
                    {
                        TextComponentTranslation itextcomponent = new TextComponentTranslation("minetogether.connect.open.failed");
                        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(itextcomponent);
                    }
                });
            } else {
                TextComponentTranslation itextcomponent = new TextComponentTranslation("minetogether.connect.open.failed");
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(itextcomponent);
            }
        });
    }
}
