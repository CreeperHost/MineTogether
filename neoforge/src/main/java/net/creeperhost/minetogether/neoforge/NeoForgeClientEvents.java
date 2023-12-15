package net.creeperhost.minetogether.neoforge;

import net.creeperhost.minetogether.gui.MTTextures;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * Created by brandon3055 on 01/10/2023
 */
public class NeoForgeClientEvents {

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(NeoForgeClientEvents::registerReloadListeners);
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(MTTextures.getAtlasHolder());
    }
}
