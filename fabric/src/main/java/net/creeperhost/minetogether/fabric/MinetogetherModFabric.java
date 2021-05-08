package net.creeperhost.minetogether.fabric;

import net.creeperhost.minetogether.MineTogether;
import net.fabricmc.api.ModInitializer;

public class MinetogetherModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MineTogether.init();
        MineTogether.clientInit();
    }
}
