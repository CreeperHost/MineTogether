package net.creeperhost.minetogether.serverstuffs;

public class ClientProxy implements IServerProxy
{
    @Override
    public boolean killWatchdog()
    {
        return false;
    }

    @Override
    public void resuscitateWatchdog() {}

    @Override
    public boolean needsToBeKilled()
    {
        return false;
    }
}
