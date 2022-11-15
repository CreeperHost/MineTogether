package net.creeperhost.minetogether.mixin.connect;

import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin (IntegratedServer.class)
public interface IntegratedServerAccessor {

    @Accessor ("publishedPort")
    void setPublishedPort(int port);
}
