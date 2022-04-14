package net.creeperhost.minetogether.fabric;

import net.creeperhost.minetogether.MineTogetherCommon;
import net.fabricmc.api.ModInitializer;

public class MineTogetherModFabric implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        MineTogetherCommon.init();
    }
}
