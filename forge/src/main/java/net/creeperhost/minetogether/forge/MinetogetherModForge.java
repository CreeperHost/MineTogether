package net.creeperhost.minetogether.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import net.creeperhost.minetogether.Minetogether;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Minetogether.MOD_ID)
public class MinetogetherModForge
{
    public MinetogetherModForge()
    {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Minetogether.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Minetogether.init();

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::clientSetup);
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        Minetogether.clientInit();
    }
}
