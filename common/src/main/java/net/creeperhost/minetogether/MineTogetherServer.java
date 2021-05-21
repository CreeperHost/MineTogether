package net.creeperhost.minetogether;

public class MineTogetherServer
{
    public static void init()
    {
        killWatchDog();
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
