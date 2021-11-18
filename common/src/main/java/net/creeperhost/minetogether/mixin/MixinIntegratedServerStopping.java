package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.module.connect.ConnectModule;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServerStopping {
    @Inject(at = @At("TAIL"), method = "stopServer()V")
    public void onServerStopped(CallbackInfo ci) {
        ConnectModule.onServerStopping((IntegratedServer)(Object)this);
    }
}
