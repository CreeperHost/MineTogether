package net.creeperhost.minetogether.fabric;

import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.orderform.OrderForm;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

/**
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MineTogether.init();

        if (Platform.getEnv() == EnvType.CLIENT) {
            clientInit();
        }
    }

    private void clientInit() {
        //We need this because INIT_POST from architectury only works in the initial init event.
        //It does not fire on re-init, e.g. when window is resized. I would consider this a bug in architectury.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> OrderForm.onScreenPostInit(screen));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> MineTogetherChat.onScreenPostInit(screen));
    }
}
