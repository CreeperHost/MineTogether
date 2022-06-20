package net.creeperhost.minetogether.fabric;

import net.creeperhost.minetogether.MineTogether;
import net.fabricmc.api.ModInitializer;

/**
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MineTogether.init();
    }
}
