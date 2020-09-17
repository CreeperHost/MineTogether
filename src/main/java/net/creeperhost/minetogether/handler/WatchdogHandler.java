package net.creeperhost.minetogether.handler;

import net.creeperhost.minetogether.MineTogether;
import net.minecraft.server.ServerPropertiesProvider;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerHangWatchdog;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WatchdogHandler
{
    public WatchdogHandler()
    {
        killWatchdog();
    }
    
    public boolean killWatchdog()
    {
        Thread watchdogThread = MineTogether.getThreadByName("Server Watchdog");
        if (watchdogThread == null)
        {
            return true;
        }
        
        try
        {
            if (isEnabled())
            {
                MineTogether.logger.info("We're about to kill the Server Watchdog. Don't worry, we'll resuscitate it! The next error is normal.");
                
                watchdogThread.interrupt();
                return true;
            }
            return false;
        } catch (Throwable e)
        {
            return false;
        }
    }
    
    //One this we will bring it back maybe??
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
    
    private boolean isEnabled()
    {
        Path path = Paths.get("server.properties");
        ServerPropertiesProvider serverpropertiesprovider = new ServerPropertiesProvider(path);
        if (serverpropertiesprovider.getProperties().maxTickTime >= 0)
        {
            return false;
        }
        return true;
    }
}
