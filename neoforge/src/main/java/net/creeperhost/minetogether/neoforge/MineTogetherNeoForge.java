package net.creeperhost.minetogether.neoforge;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.orderform.OrderForm;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import static net.creeperhost.minetogether.MineTogether.MOD_ID;

/**
 * Created by covers1624 on 20/6/22.
 */
@Mod (MOD_ID)
public class MineTogetherNeoForge {

    public MineTogetherNeoForge() {
        MineTogether.init();

        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(this::clientInit);
            NeoForgeClientEvents.init();
        }
    }

    private void clientInit(ScreenEvent.Init.Post event) {
        //We need this because INIT_POST from architectury only works in the initial init event.
        //It does not fire on re-init, e.g. when window is resized. I would consider this a bug in architectury.
        OrderForm.onScreenPostInit(event.getScreen());
        MineTogetherChat.onScreenPostInit(event.getScreen());
    }
}
