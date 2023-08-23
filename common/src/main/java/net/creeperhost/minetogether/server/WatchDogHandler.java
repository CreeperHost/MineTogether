package net.creeperhost.minetogether.server;

import net.creeperhost.minetogether.MineTogetherServer;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerWatchdog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WatchDogHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    public static boolean watchDogActive = true;

    public static void killWatchDog() {
        if (!watchDogActive) return;

        Thread watchdogThread = getThreadByName("Server Watchdog");
        if (watchdogThread == null) {
            LOGGER.info("Watchdog thread not found");
            return;
        }

        try {
            if (watchdogThread != null && watchdogThread.isAlive()) {
                LOGGER.info("We're about to kill the Server Watchdog. Don't worry, we'll resuscitate it! The next error is normal.");
                watchDogActive = false;
                watchdogThread.interrupt();
            }
        } catch (Exception ignored) {
        }
    }

    public static void resuscitateWatchdog() {
        if (!(MineTogetherServer.minecraftServer instanceof DedicatedServer)) return;
        DedicatedServer server = (DedicatedServer) MineTogetherServer.minecraftServer;
        if (server.getMaxTickLength() > 0L) {
            Thread thread2 = new Thread(new ServerWatchdog(server));
            thread2.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LogManager.getLogger()));
            thread2.setName("Server Watchdog");
            thread2.setDaemon(true);
            thread2.start();
            watchDogActive = true;
        }
    }

    public static Thread getThreadByName(String threadName) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().equals(threadName)) {
                return thread;
            }
        }
        return null;
    }
}
