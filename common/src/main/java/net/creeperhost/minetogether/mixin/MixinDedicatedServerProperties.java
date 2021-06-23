package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.MineTogetherServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public class MixinDedicatedServerProperties
{
    @Inject(at=@At("TAIL"), method="<init>")
    public void DedicatedServerProperties(Properties properties, RegistryAccess registryAccess, CallbackInfo ci)
    {
        properties.putIfAbsent("discoverability", "unlisted");
        properties.putIfAbsent("displayname", "Fill this in if you have set the server to public!");
        MineTogetherServer.discoverability = properties.getProperty("discoverability", "unlisted");
        MineTogetherServer.displayName = properties.getProperty("displayname", "Fill this in if you have set the server to public!");
        MineTogetherServer.server_ip = properties.getProperty("server-ip", "");
    }
}