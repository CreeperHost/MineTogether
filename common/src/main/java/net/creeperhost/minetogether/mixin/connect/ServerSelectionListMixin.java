package net.creeperhost.minetogether.mixin.connect;

import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.gui.ServerListAppender;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by brandon3055 on 21/04/2023
 */
@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin {

    private ServerSelectionList getThis() {
        return (ServerSelectionList) (Object) this;
    }

    @Mutable
    @Shadow
    @Final
    private static Component SCANNING_LABEL;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(CallbackInfo ci) {
        SCANNING_LABEL = ConnectHandler.isEnabled() ? new TranslatableComponent("minetogether.connect.scan") : new TranslatableComponent("minetogether.connect.scan.offline");
    }


    @Inject (at = @At("TAIL"), method = "refreshEntries()V")
    public void onEntriesRefresh(CallbackInfo ci) {
        ServerListAppender.INSTANCE.addEntries();
    }
}
