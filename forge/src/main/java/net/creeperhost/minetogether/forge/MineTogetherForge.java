package net.creeperhost.minetogether.forge;

import dev.architectury.platform.forge.EventBuses;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.orderform.OrderForm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
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

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(this::clientInit));
    }

    private void clientInit(ScreenEvent.Init.Post event) {
        //We need this because INIT_POST from architectury only works in the initial init event.
        //It does not fire on re-init, e.g. when window is resized. I would consider this a bug in architectury.
        OrderForm.onScreenPostInit(event.getScreen());
        MineTogetherChat.onScreenPostInit(event.getScreen());
    }
}
