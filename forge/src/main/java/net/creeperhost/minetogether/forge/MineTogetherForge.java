package net.creeperhost.minetogether.forge;

import dev.architectury.platform.forge.EventBuses;
import net.creeperhost.minetogether.MineTogether;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static net.creeperhost.minetogether.MineTogether.MOD_ID;

/**
 * Created by covers1624 on 20/6/22.
 */
@Mod (MOD_ID)
public class MineTogetherForge {

    public MineTogetherForge() {
        EventBuses.registerModEventBus(MineTogether.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        MineTogether.init();
    }
}
