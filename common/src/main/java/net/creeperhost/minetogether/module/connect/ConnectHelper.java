package net.creeperhost.minetogether.module.connect;

import net.creeperhost.minetogether.mixin.MixinIntegratedServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ConnectHelper
{

    public static boolean isEnabled = false;

    public static boolean isShared()
    {
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        return isShared(integratedServer);
    }

    public static boolean isShared(IntegratedServer integratedServer)
    {
        if (integratedServer == null)
        {
            return false;
        }
        return integratedServer.getPort() == 42069 && integratedServer.isPublished();
    }

    public static void shareToFriends(GameType type, boolean allowCheats)
    {
        CompletableFuture.runAsync(() ->
        {
            net.creeperhost.minetogether.module.connect.ConnectHandler.Response response = net.creeperhost.minetogether.module.connect.ConnectHandler.openBlocking();
            if (response.isSuccess())
            {
                IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
                Objects.requireNonNull(integratedServer).submit(() ->
                {
                    try
                    {
                        int port = 42069;
                        System.setProperty("java.net.preferIPv4Stack", "false"); // no tears, not only ipv4
                        integratedServer.getConnection().startTcpServerListener(null, port); // make localhost only
                        ((MixinIntegratedServer) integratedServer).setPublishedPort(port);
                        integratedServer.setPort(port);
                        integratedServer.getPlayerList().setOverrideGameMode(type);
                        integratedServer.getPlayerList().setAllowCheatsForAllPlayers(allowCheats);
                        int i = integratedServer.getProfilePermissions(Minecraft.getInstance().player.getGameProfile());
                        Minecraft.getInstance().player.setPermissionLevel(i);
                        for (ServerPlayer serverplayerentity : integratedServer.getPlayerList().getPlayers())
                        {
                            integratedServer.getCommands().sendCommands(serverplayerentity);
                        }


                        TranslatableComponent itextcomponent = new TranslatableComponent("minetogether.connect.open.success");

                        Minecraft.getInstance().updateTitle();

                        Minecraft.getInstance().gui.getChat().addMessage(itextcomponent);
                    } catch (IOException var6)
                    {
                        TranslatableComponent itextcomponent = new TranslatableComponent("minetogether.connect.open.failed");
                        Minecraft.getInstance().gui.getChat().addMessage(itextcomponent);
                    }
                });
            }
            else
            {
                TranslatableComponent itextcomponent = new TranslatableComponent("minetogether.connect.open.failed", response.getMessage());
                Minecraft.getInstance().gui.getChat().addMessage(itextcomponent);
            }
        });
    }

}
