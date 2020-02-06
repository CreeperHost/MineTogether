package net.creeperhost.minetogether.handler;

import net.creeperhost.minetogether.MineTogether;

public class WatchdogHandler
{
    private boolean needsToBeKilled = true;
    private boolean watchdogKilled = false;
    private boolean watchdogChecked = false;

    private void killWatchdog()
    {
        if (!watchdogChecked)
        {
            needsToBeKilled = MineTogether.serverProxy.needsToBeKilled();
            watchdogChecked = true;
        }
        if (!watchdogKilled && needsToBeKilled)
        {
            watchdogKilled = MineTogether.serverProxy.killWatchdog();
        }
    }

    private void resuscitateWatchdog()
    {
        if (watchdogKilled && needsToBeKilled)
        {
            MineTogether.serverProxy.resuscitateWatchdog();
            watchdogKilled = false;
        }
    }
}
