package net.creeperhost.minetogether.api;

import java.util.ArrayList;

/**
 * Created by Aaron on 09/05/2017.
 */
public class CreeperHostAPI
{
    public static ArrayList<IServerHost> implementations = new ArrayList<IServerHost>();

    public static void registerImplementation(IServerHost plugin)
    {
        implementations.add(plugin);
    }

    public static ArrayList<IServerHost> getImplementations()
    {
        return implementations;
    }
}
