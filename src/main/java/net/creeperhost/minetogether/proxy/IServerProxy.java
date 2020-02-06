package net.creeperhost.minetogether.proxy;

public interface IServerProxy
{
    boolean killWatchdog();

    void resuscitateWatchdog();

    boolean needsToBeKilled();
}
