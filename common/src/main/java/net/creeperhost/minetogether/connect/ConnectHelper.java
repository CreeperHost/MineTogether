package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.mixin.connect.MixinIntegratedServer;
import net.creeperhost.minetogetherconnect.ConnectMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ConnectHelper {

    public static boolean isEnabled = false;

    public static boolean isShared() {
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        return isShared(integratedServer);
    }

    public static boolean isShared(MinecraftServer integratedServer) {
        if (integratedServer == null) {
            return false;
        }
        return !integratedServer.isDedicatedServer() && integratedServer.getPort() == 42069 && integratedServer.isPublished();
    }

    public static void shareToFriends(GameType type, boolean allowCheats) {
        CompletableFuture.runAsync(() ->
        {
            try {
                ConnectHandler.openCallback((message) -> {
                    if (message.equals("CLOSED123")) {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("MineTogether Connect: An error occurred and you are no longer listening for new friend connections. Please reload your world and open to friends again to fix this!"));
                        ConnectMain.close();
                    } else {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("MineTogether Connect: " + message));
                    }
                }, (response) -> {
                    if (response.isSuccess()) {
                        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
                        Objects.requireNonNull(integratedServer).submit(() ->
                        {
                            try {
                                int port = 42069;
                                System.setProperty("java.net.preferIPv4Stack", "false"); // no tears, not only ipv4
                                integratedServer.getConnection().startTcpServerListener(null, port); // make localhost only
                                ((MixinIntegratedServer) integratedServer).setPublishedPort(port);
                                integratedServer.getPlayerList().getMaxPlayers();
                                integratedServer.setPort(port);
                                integratedServer.setDefaultGameType(type);
//                                integratedServer.getPlayerList().setOverrideGameMode(type);
                                integratedServer.getPlayerList().setAllowCheatsForAllPlayers(allowCheats);
                                int i = integratedServer.getProfilePermissions(Minecraft.getInstance().player.getGameProfile());
                                Minecraft.getInstance().player.setPermissionLevel(i);
                                for (ServerPlayer serverplayerentity : integratedServer.getPlayerList().getPlayers()) {
                                    integratedServer.getCommands().sendCommands(serverplayerentity);
                                }

                                Minecraft.getInstance().updateTitle();

                                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("minetogether.connect.open.success"));
                            } catch (IOException var6) {
                                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("minetogether.connect.open.failed"));
                            }
                        });
                    } else {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("minetogether.connect.open.failed", response.getMessage()));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
