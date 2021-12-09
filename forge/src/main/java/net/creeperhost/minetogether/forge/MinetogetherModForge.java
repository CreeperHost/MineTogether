package net.creeperhost.minetogether.forge;

import dev.architectury.platform.forge.EventBuses;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherCommon;
import net.creeperhost.minetogether.lib.MineTogether;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class MinetogetherModForge
{
    public MinetogetherModForge()
    {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Constants.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        MineTogetherCommon.init();

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::serverSetup);
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        MineTogetherCommon.clientInit();
    }

    public void serverSetup(FMLDedicatedServerSetupEvent event)
    {
        MineTogetherCommon.serverInit();
    }
}
