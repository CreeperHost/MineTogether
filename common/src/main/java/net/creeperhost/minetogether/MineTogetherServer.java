package net.creeperhost.minetogether;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.creeperhost.minetogether.server.Discoverability;
import net.creeperhost.minetogether.server.PregenHandler;
import net.creeperhost.minetogether.server.ServerListThread;
import net.creeperhost.minetogether.server.commands.MTCommands;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

/**
 * Initialize on a dedicated server.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherServer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static int inviteId;
    public static String discoverability = "unlisted";
    public static String displayName = "";
    public static String serverIp = "";

    public static MinecraftServer minecraftServer = null;

    public static void init() {
        LOGGER.info("Initializing MineTogetherServer!");

        CommandRegistrationEvent.EVENT.register(MTCommands::registerCommand);
        TickEvent.SERVER_POST.register(PregenHandler::onWorldTick);
        PlayerEvent.PLAYER_JOIN.register(PregenHandler::onPlayerJoin);

        PregenHandler.deserializePreload();

        LifecycleEvent.SERVER_STARTED.register(MineTogetherServer::serverStarted);
    }

    private static void serverStarted(MinecraftServer server) {
        minecraftServer = server;
        if (server instanceof DedicatedServer) {
            startServerListThread(server);
        }
    }

    private static void startServerListThread(MinecraftServer server) {
        String projectId = ModPackInfo.base64FTBID;
        Discoverability discoverability;
        LOGGER.info("Current discoverability: {}", MineTogetherServer.discoverability);
        try {
            discoverability = Discoverability.valueOf(MineTogetherServer.discoverability.toUpperCase(Locale.ROOT));
        } catch (Throwable ex) {
            LOGGER.error("Malformed discoverability in server.properties.");
            return;
        }
        if (discoverability == Discoverability.UNLISTED) {
            LOGGER.info("Server set to unlisted. Disabling server listing.");
            return;
        }

        if (projectId.isEmpty()) {
            LOGGER.info("Unable to find version.json. Assuming Curse modpack.");
            projectId = ModPackInfo.curseID;
        }

        if (projectId.isEmpty()) {
            LOGGER.info("Unable to find project ID. Server listing disabled.");
            return;
        }

        new ServerListThread(serverIp, displayName, projectId, server.getPort(), discoverability)
                .start();
    }
}
