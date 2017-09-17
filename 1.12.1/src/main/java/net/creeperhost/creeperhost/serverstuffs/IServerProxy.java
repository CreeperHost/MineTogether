package net.creeperhost.creeperhost.serverstuffs;

public interface IServerProxy
{
    boolean killWatchdog();
    void resuscitateWatchdog();
    boolean needsToBeKilled();
}
