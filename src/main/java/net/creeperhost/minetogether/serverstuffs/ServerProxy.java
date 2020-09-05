package net.creeperhost.minetogether.serverstuffs;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerHangWatchdog;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

import static net.minecraftforge.fml.common.FMLCommonHandler.instance;

public class ServerProxy implements IServerProxy
{
    private Field field = null;
    private Field server = null;
    
    @Override
    public boolean killWatchdog()
    {
        if (field == null)
        {
            try
            {
                field = Thread.class.getDeclaredField("target");
                field.setAccessible(true);
                server = ReflectionHelper.findField(ServerHangWatchdog.class, "server", "field_180249_b", "");
                server.setAccessible(true);
            } catch (Throwable e)
            {
                return false;
            }
        }
        Thread watchdogThread = CreeperHostServer.getThreadByName("Server Watchdog");
        if (watchdogThread == null)
        {
            return true;
        }
        
        CreeperHostServer.logger.info("We're about to kill the Server Watchdog. Don't worry, we'll resuscitate it! The next error is normal.");
        
        try
        {
            ServerHangWatchdog target = (ServerHangWatchdog) field.get(watchdogThread);
            server.set(target, null);
            watchdogThread.interrupt();
            return true;
        } catch (Throwable e) { return false; }
    }
    
    @Override
    public void resuscitateWatchdog()
    {
        DedicatedServer server = (DedicatedServer) instance().getMinecraftServerInstance();
        if (server.getMaxTickTime() > 0L)
        {
            Thread thread1 = new Thread(new ServerHangWatchdog(server));
            thread1.setName("Server Watchdog");
            thread1.setDaemon(true);
            thread1.start();
            CreeperHostServer.logger.info("Performing CPR. Server Watchdog is alive again!");
        }
    }
    
    @Override
    public boolean needsToBeKilled()
    {
        return ((DedicatedServer) instance().getMinecraftServerInstance()).getMaxTickTime() > 0;
    }
}
