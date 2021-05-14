package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.client.MinecraftForgeClient;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ConnectHelper {

    public static boolean isEnabled = false;

    public static boolean isShared() {
        IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
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
                IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
                Objects.requireNonNull(integratedServer).deferTask(() -> {
                    try
                    {
                        int port = 42069;
                        System.setProperty("java.net.preferIPv4Stack", "false"); // no tears, not only ipv4
                        integratedServer.setOnlineMode(false);
                        integratedServer.getNetworkSystem().addEndpoint(null, port); // make localhost only
                        integratedServer.serverPort = port;
                        integratedServer.setServerPort(port); // but of course, this doesn't set integratedserver.port... why would it
                        integratedServer.getPlayerList().setGameType(type);
                        integratedServer.getPlayerList().setCommandsAllowedForAll(allowCheats);
                        int i = integratedServer.getPermissionLevel(Minecraft.getInstance().player.getGameProfile());
                        Minecraft.getInstance().player.setPermissionLevel(i);
                        for (ServerPlayerEntity serverplayerentity : integratedServer.getPlayerList().getPlayers()) {
                            integratedServer.getCommandManager().send(serverplayerentity);
                        }


                        ITextComponent itextcomponent = new TranslationTextComponent("minetogether.connect.open.success");

                        Minecraft.getInstance().setDefaultMinecraftTitle();

                        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(itextcomponent);
                    }
                    catch (IOException var6)
                    {
                        return;
                    }
                });
            } else {
                ITextComponent itextcomponent = new StringTextComponent("minetogether.connect.open.failed");
                Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(itextcomponent);
            }
        });
    }

}
