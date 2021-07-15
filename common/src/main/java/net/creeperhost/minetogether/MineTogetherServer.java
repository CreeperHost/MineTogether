package net.creeperhost.minetogether;

import com.mojang.brigadier.CommandDispatcher;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import net.creeperhost.minetogether.commands.CommandInvite;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.threads.MineTogetherServerThread;
import net.creeperhost.minetogether.verification.ModPackVerifier;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
        //This is just a test and needs to be moved
        killWatchDog();
        CommandRegistrationEvent.EVENT.register(MineTogetherServer::registerCommand);
    }

    private static void registerCommand(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, Commands.CommandSelection commandSelection)
    {
        commandSourceStackCommandDispatcher.register(CommandInvite.register());
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

            //            System.out.println(ipaddress);
            //            System.out.println(packID);

            String base64 = Base64.getEncoder().encodeToString((String.valueOf(ipaddress) + String.valueOf(packID)).getBytes());
            //            System.out.println(base64);
            return base64;

        } catch (Exception ignored)
        {
        }
        return "";
    }

    public static void killWatchDog()
    {
        Thread watchdogThread = getThreadByName("Server Watchdog");
        if (watchdogThread == null)
        {
            MineTogether.logger.info("Watchdog thread not found");
            return;
        }

        try
        {
            MineTogether.logger.info("We're about to kill the Server Watchdog. Don't worry, we'll resuscitate it! The next error is normal.");
            watchdogThread.interrupt();
        } catch (Exception ignored)
        {
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
