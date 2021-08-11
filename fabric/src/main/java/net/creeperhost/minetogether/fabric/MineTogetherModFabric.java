package net.creeperhost.minetogether.fabric;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

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
