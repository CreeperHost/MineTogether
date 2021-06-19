package net.creeperhost.minetogether;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.threads.MineTogetherServerThread;
import net.creeperhost.minetogether.verification.ModPackVerifier;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.minecraft.server.MinecraftServer;

public class MineTogetherServer
{
    public static boolean serverOn = false;
    public static String secret = "";
    public static int updateID;
    public static String discoverability = "";
    public static String displayName = "";
    public static String server_ip = "";
    public static MinecraftServer minecraftServer = null;

    public static void init()
    {
        serverOn = true;
        SignatureVerifier signatureVerifier = new SignatureVerifier();
        ModPackVerifier modPackVerifier = new ModPackVerifier();
        modPackVerifier.verify();
        secret = signatureVerifier.verify();
        //This is just a test and needs to be moved
        killWatchDog();
    }

    public static void serverStarted(MinecraftServer minecraftServer)
    {
        MineTogetherServer.minecraftServer = minecraftServer;
        buildMineTogetherServerThread();
    }

    public static void buildMineTogetherServerThread()
    {
        String projectID = MineTogether.base64;
        MineTogetherServerThread.Discoverability discover;
        try
        {
            MineTogether.logger.info("Current discoverability: " + discoverability);

            discover = MineTogetherServerThread.Discoverability.valueOf(discoverability.toUpperCase());
        }
        catch (Exception e)
        {
            MineTogether.logger.error("Failed read discoverability from server.properties");
            return;
        }

        if(projectID.isEmpty())
        {
            MineTogether.logger.info("Unable to find version.json, Assuming Curse pack");
            projectID = Config.getInstance().curseProjectID;
        }

        Config defaultConfig = new Config();
        if(projectID.isEmpty() || projectID.equals(defaultConfig.getCurseProjectID()))
        {
            MineTogether.logger.error("Unable to find project ID, Not adding to server list");
            return;
        }

        MineTogetherServerThread.startMineTogetherServerThread(server_ip, displayName, projectID, minecraftServer.getPort(), discover);
    }

    public static void killWatchDog()
    {
        Thread watchdogThread = getThreadByName("Server Watchdog");
        if(watchdogThread == null)
        {
            MineTogether.logger.info("Watchdog thread not found");
            return;
        }

        try
        {
            MineTogether.logger.info("We're about to kill the Server Watchdog. Don't worry, we'll resuscitate it! The next error is normal.");
            watchdogThread.interrupt();
        } catch (Exception ignored) {}
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
