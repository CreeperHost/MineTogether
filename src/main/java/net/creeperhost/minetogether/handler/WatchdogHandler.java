package net.creeperhost.minetogether.handler;

import net.creeperhost.minetogether.MineTogether;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerHangWatchdog;

public class WatchdogHandler
{
    public WatchdogHandler()
    {
        //TODO config this
        killWatchdog();
    }

    public boolean killWatchdog()
    {
        Thread watchdogThread = MineTogether.getThreadByName("Server Watchdog");
        if (watchdogThread == null)
        {
            return true;
        }

        MineTogether.logger.info("We're about to kill the Server Watchdog. Don't worry, we'll resuscitate it! The next error is normal.");

        try
        {
            watchdogThread.interrupt();
            return true;
        } catch (Throwable e)
        {
            return false;
        }
    }

    private void resuscitateWatchdog()
    {
        DedicatedServer server = (DedicatedServer) MineTogether.server;
        if (server.getMaxTickTime() > 0L)
        {
            Thread thread1 = new Thread(new ServerHangWatchdog(server));
            thread1.setName("Server Watchdog");
            thread1.setDaemon(true);
            thread1.start();
            MineTogether.logger.info("Performing CPR. Server Watchdog is alive again!");
        }
    }
}
