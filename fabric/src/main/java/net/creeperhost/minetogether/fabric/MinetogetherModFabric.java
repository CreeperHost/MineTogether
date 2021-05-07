package net.creeperhost.minetogether.fabric;

import net.creeperhost.minetogether.Minetogether;
import net.fabricmc.api.ModInitializer;

public class MinetogetherModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Minetogether.init();
        Minetogether.clientInit();
    }
}
