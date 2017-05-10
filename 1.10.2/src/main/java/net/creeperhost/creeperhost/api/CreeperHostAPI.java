package net.creeperhost.creeperhost.api;

import net.minecraftforge.fml.common.Loader;

/**
 * Created by Aaron on 09/05/2017.
 */
public class CreeperHostAPI
{
    public static void registerImplementation(IServerHost plugin) {
        if (Loader.isModLoaded("creeperhost"))
        {
            Object mod = Loader.instance().getIndexedModList().get("creeperhost").getMod();
            ((ICreeperHostMod) mod).registerImplementation(plugin);
        }
    }
}
