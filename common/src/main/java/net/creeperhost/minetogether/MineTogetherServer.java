package net.creeperhost.minetogether;

import com.mojang.brigadier.CommandDispatcher;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.event.events.TickEvent;
import net.creeperhost.minetogether.commands.CommandInvite;
import net.creeperhost.minetogether.commands.CommandPregen;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.handler.PregenHandler;
import net.creeperhost.minetogether.threads.MineTogetherServerThread;
import net.creeperhost.minetogether.verification.ModPackVerifier;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerWatchdog;
import net.minecraft.server.level.ServerPlayer;

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
    public static boolean watchDogActive = true;

    public static void init()
    {
        serverOn = true;
        SignatureVerifier signatureVerifier = new SignatureVerifier();
        ModPackVerifier modPackVerifier = new ModPackVerifier();
        packID = modPackVerifier.verify();
        secret = signatureVerifier.verify();
        CommandRegistrationEvent.EVENT.register(MineTogetherServer::registerCommand);
        TickEvent.ServerWorld.SERVER_POST.register(MineTogetherServer::onServerTick);
        PlayerEvent.PLAYER_JOIN.register(MineTogetherServer::onPlayerJoin);
        PregenHandler.deserializePreload();
    }

    private static void onPlayerJoin(ServerPlayer serverPlayer)
    {
        if(serverPlayer != null && PregenHandler.isPreGenerating() && PregenHandler.shouldKickPlayer)
        {
            String remainingTime = PregenHandler.getActiveTask() != null ? PregenHandler.getTimeRemaining(PregenHandler.getActiveTask()) : "";

            serverPlayer.connection.disconnect(new TranslatableComponent("Server is still pre-generating!\n" + remainingTime + " Remaining"));
            MineTogether.logger.error("Kicked player " + serverPlayer.getName() + " as still pre-generating");
        }
    }

    private static void onServerTick(MinecraftServer minecraftServer)
    {
        PregenHandler.onWorldTick(minecraftServer);
    }

    private static void registerCommand(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, Commands.CommandSelection commandSelection)
    {
        commandSourceStackCommandDispatcher.register(CommandInvite.register());
        commandSourceStackCommandDispatcher.register(CommandPregen.register());
    }

    public static void serverStarted(MinecraftServer minecraftServer)
    {
        MineTogetherServer.minecraftServer = minecraftServer;
        if (minecraftServer instanceof DedicatedServer)
        {
            buildMineTogetherServerThread();
            createHash(minecraftServer);
        }
    }

    public static void buildMineTogetherServerThread()
    {
        String projectID = MineTogether.base64;
        MineTogetherServerThread.Discoverability discover;
        try
        {
            MineTogether.logger.info("Current discoverability: " + discoverability);

            discover = MineTogetherServerThread.Discoverability.valueOf(discoverability.toUpperCase());
        } catch (Exception e)
        {
            MineTogether.logger.error("Failed read discoverability from server.properties");
            return;
        }

        if (projectID.isEmpty())
        {
            MineTogether.logger.info("Unable to find version.json, Assuming Curse pack");
            projectID = Config.getInstance().curseProjectID;
        }

        Config defaultConfig = new Config();
        if (projectID.isEmpty() || projectID.equals(defaultConfig.getCurseProjectID()))
        {
            MineTogether.logger.error("Unable to find project ID, Not adding to server list");
            return;
        }

        MineTogetherServerThread.startMineTogetherServerThread(server_ip, displayName, projectID, minecraftServer.getPort(), discover);
    }

    public static String createHash(MinecraftServer server)
    {
        try
        {
            URL url = new URL("https://api.callbacks.io/ip");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String ipaddress = bufferedReader.readLine();
            bufferedReader.close();

            String packID = MineTogetherServer.packID;

            String base64 = Base64.getEncoder().encodeToString((String.valueOf(ipaddress) + String.valueOf(packID)).getBytes());
            return base64;

        } catch (Exception ignored)
        {
        }
        return "";
    }

    public static void killWatchDog()
    {
        if(!watchDogActive) return;

        Thread watchdogThread = getThreadByName("Server Watchdog");
        if (watchdogThread == null)
        {
            MineTogether.logger.info("Watchdog thread not found");
            return;
        }

        try
        {
            if(watchdogThread != null && watchdogThread.isAlive())
            {
                MineTogether.logger.info("We're about to kill the Server Watchdog. Don't worry, we'll resuscitate it! The next error is normal.");
                watchDogActive = false;
                watchdogThread.interrupt();
            }
        } catch (Exception ignored)
        {
        }
    }

    public static void resuscitateWatchdog()
    {
        if(!(minecraftServer instanceof DedicatedServer)) return;
        DedicatedServer server = (DedicatedServer) minecraftServer;
        if (server.getMaxTickLength() > 0L)
        {
            Thread thread2 = new Thread(new ServerWatchdog(server));
            thread2.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(MineTogether.logger));
            thread2.setName("Server Watchdog");
            thread2.setDaemon(true);
            thread2.start();
            watchDogActive = true;
        }
    }

    public static Thread getThreadByName(String threadName)
    {
        for (Thread thread : Thread.getAllStackTraces().keySet())
        {
            if (thread.getName().equals(threadName)) return thread;
        }
        return null;
    }
}
