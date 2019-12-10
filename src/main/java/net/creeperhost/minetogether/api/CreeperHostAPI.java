package net.creeperhost.minetogether.api;

/**
 * Created by Aaron on 09/05/2017.
 */
public class CreeperHostAPI
{
    public static void registerImplementation(IServerHost plugin)
    {
        //Why was this ever done like this???
//        if (Loader.isModLoaded("minetogether"))
//        {
//            Object mod = Loader.instance().getIndexedModList().get("minetogether").getMod();
//            ((ICreeperHostMod) mod).registerImplementation(plugin);
//        }
    }
}
