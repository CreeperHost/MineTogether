package net.creeperhost.minetogether.serverstuffs;

public interface IServerProxy
{
    boolean killWatchdog();

    void resuscitateWatchdog();

    boolean needsToBeKilled();
}
