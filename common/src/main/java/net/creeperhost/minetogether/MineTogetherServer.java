package net.creeperhost.minetogether;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.creeperhost.minetogether.commands.MTCommands;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.handler.PregenHandler;
import net.creeperhost.minetogether.threads.MineTogetherServerThread;
import net.creeperhost.minetogether.verification.ModPackVerifier;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;

public class MineTogetherServer
{
    public static boolean serverOn = false;
    public static String secret = "";
    public static int updateID;
    public static String discoverability = "";
    public static String displayName = "";
    public static String server_ip = "";
    public static MinecraftServer minecraftServer = null;
    public static String packID = "-1";

    public static void init()
    {
        serverOn = true;
        SignatureVerifier signatureVerifier = new SignatureVerifier();
        ModPackVerifier modPackVerifier = new ModPackVerifier();
        packID = modPackVerifier.verify();
        secret = signatureVerifier.verify();
        CommandRegistrationEvent.EVENT.register(MTCommands::registerCommand);
        TickEvent.SERVER_POST.register(PregenHandler::onWorldTick);
        PlayerEvent.PLAYER_JOIN.register(PregenHandler::onPlayerJoin);
        LifecycleEvent.SERVER_STARTED.register(MineTogetherServer::serverStarted);
        LifecycleEvent.SERVER_STOPPING.register(MineTogetherServer::serverStopped);
        PregenHandler.deserializePreload();

        Runtime.getRuntime().addShutdownHook(new Thread(MineTogetherServerThread::stopThread));
    }

    private static void serverStopped(MinecraftServer minecraftServer)
    {
        MineTogetherServerThread.stopThread();
    }

    public static void serverStarted(MinecraftServer minecraftServer)
    {
        MineTogetherServer.minecraftServer = minecraftServer;
        if (minecraftServer instanceof DedicatedServer)
        {
            buildMineTogetherServerThread();
            createHash();
        }
    }

    public static void buildMineTogetherServerThread()
    {
        String projectID = MineTogetherCommon.base64;
        MineTogetherServerThread.Discoverability discover;
        try
        {
            MineTogetherCommon.logger.info("Current discoverability: " + discoverability);

            discover = MineTogetherServerThread.Discoverability.valueOf(discoverability.toUpperCase());
        } catch (Exception e)
        {
            MineTogetherCommon.logger.error("Failed read discoverability from server.properties");
            return;
        }

        if (projectID.isEmpty())
        {
            MineTogetherCommon.logger.info("Unable to find version.json, Assuming Curse pack");
            projectID = Config.getInstance().curseProjectID;
        }

        Config defaultConfig = new Config();
        if (projectID.isEmpty() || projectID.equals(defaultConfig.getCurseProjectID()))
        {
            MineTogetherCommon.logger.error("Unable to find project ID, Not adding to server list");
            return;
        }

        if(discover == MineTogetherServerThread.Discoverability.UNLISTED) return;

        MineTogetherServerThread.startMineTogetherServerThread(server_ip, displayName, projectID, minecraftServer.getPort(), discover);
    }

    public static String createHash()
    {
        try
        {
            URL url = new URL("https://api.callbacks.io/ip");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String ipaddress = bufferedReader.readLine();
            bufferedReader.close();

            String packID = MineTogetherServer.packID;

            return Base64.getEncoder().encodeToString((String.valueOf(ipaddress) + String.valueOf(packID)).getBytes());

        } catch (Exception e)
        {
            MineTogetherCommon.sentryException(e);
        }
        return "";
    }
}
