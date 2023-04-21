package net.creeperhost.minetogether.mixin.connect;

import net.creeperhost.minetogether.connectv2.ConnectHandlerV2;
import net.creeperhost.minetogether.connectv2.gui.ServerListAppender;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
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
        SCANNING_LABEL = ConnectHandlerV2.isEnabled() ? Component.translatable("minetogether.connect.scan") : Component.translatable("minetogether.connect.scan.offline");
    }


    @Inject (at = @At("TAIL"), method = "refreshEntries()V")
    public void onEntriesRefresh(CallbackInfo ci) {
        ServerListAppender.INSTANCE.addEntries();
    }
}
