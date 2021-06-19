package net.creeperhost.minetogether.fabric;

import net.creeperhost.minetogether.MineTogether;
import net.fabricmc.api.ModInitializer;

public class MineTogetherModFabric implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        MineTogether.init();
        MineTogether.serverInit();
        MineTogether.clientInit();
    }
}
