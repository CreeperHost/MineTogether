package net.creeperhost.minetogether.api;

import cpw.mods.fml.common.Loader;

/**
 * Created by Aaron on 09/05/2017.
 */
public class CreeperHostAPI
{
    public static void registerImplementation(IServerHost plugin) {
        if (Loader.isModLoaded("minetogether"))
        {
            Object mod = Loader.instance().getIndexedModList().get("minetogether").getMod();
            ((ICreeperHostMod) mod).registerImplementation(plugin);
        }
    }
}
