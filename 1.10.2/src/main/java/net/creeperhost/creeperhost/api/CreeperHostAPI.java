package net.creeperhost.creeperhost.api;

import net.creeperhost.creeperhost.CreeperHost;

/**
 * Created by Aaron on 09/05/2017.
 */
public class CreeperHostAPI
{
    public static void registerImplementation(IServerHost plugin) {
        CreeperHost.instance.implementations.add(plugin);
    }
}
