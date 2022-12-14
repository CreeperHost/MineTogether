package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.mixin.connect.IntegratedServerAccessor;
import net.creeperhost.minetogetherconnect.ConnectMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ConnectHelper {
    private static final Logger LOGGER = LogManager.getLogger();

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
        Minecraft mc = Minecraft.getInstance();
        CompletableFuture.runAsync(() ->
        {
            try {
                ConnectHandler.openCallback((message) -> {
                    if (message.equals("CLOSED123")) {
                        mc.gui.getChat().addMessage(Component.literal("MineTogether Connect: An error occurred and you are no longer listening for new friend connections. Please reload your world and open to friends again to fix this!"));
                        ConnectMain.close();
                    } else {
                        mc.gui.getChat().addMessage(Component.literal("MineTogether Connect: " + message));
                    }
                }, (response) -> {
                    if (response.isSuccess()) {
                        IntegratedServer integratedServer = mc.getSingleplayerServer();
                        Objects.requireNonNull(integratedServer).submit(() ->
                        {
                            try {
                                int port = 42069;
                                System.setProperty("java.net.preferIPv4Stack", "false"); // no tears, not only ipv4
                                integratedServer.getConnection().startTcpServerListener(null, port); // make localhost only
                                ((IntegratedServerAccessor) integratedServer).setPublishedPort(port);
                                integratedServer.getPlayerList().getMaxPlayers();
                                integratedServer.setPort(port);
                                integratedServer.setDefaultGameType(type);
//                                integratedServer.getPlayerList().setOverrideGameMode(type);
                                integratedServer.getPlayerList().setAllowCheatsForAllPlayers(allowCheats);
                                int i = integratedServer.getProfilePermissions(Minecraft.getInstance().player.getGameProfile());
                                mc.player.setPermissionLevel(i);
                                for (ServerPlayer serverplayerentity : integratedServer.getPlayerList().getPlayers()) {
                                    integratedServer.getCommands().sendCommands(serverplayerentity);
                                }

                                mc.submit(() -> mc.updateTitle());

                                mc.gui.getChat().addMessage(Component.translatable("minetogether.connect.open.success"));
                            } catch (IOException var6) {
                                mc.gui.getChat().addMessage(Component.translatable("minetogether.connect.open.failed"));
                            }
                        });
                    } else {
                        mc.gui.getChat().addMessage(Component.translatable("minetogether.connect.open.failed", response.getMessage()));
                    }
                });
            } catch (Exception ex) {
                LOGGER.error("Error opening to friends.", ex);
            }
        });
    }

}
