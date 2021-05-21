package net.creeperhost.minetogether.mixin;

import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntegratedServer.class)
public interface MixinIntegratedServer
{
    @Accessor("publishedPort")
    void setPublishedPort(int port);
}
