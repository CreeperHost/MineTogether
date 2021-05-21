package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.multiplayer.CreeperHostServerEntry;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.class)
public abstract class MixinServerSelectionList {
    @Inject(at=@At("RETURN"), method= "refreshEntries()V")
    private void afterRefreshEntries(CallbackInfo info) {
        if(Config.getInstance().isMpMenuEnabled()) {
            ServerSelectionList thisFake = (ServerSelectionList)(Object) this;
            thisFake.children().add(0, new CreeperHostServerEntry(thisFake));
        }
    }
}
