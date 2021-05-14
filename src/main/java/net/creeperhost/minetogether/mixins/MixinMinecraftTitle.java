package net.creeperhost.minetogether.mixins;

import net.creeperhost.minetogether.mtconnect.ConnectHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraftTitle {
    @Inject(at = @At("RETURN"), method = "getWindowTitle()Ljava/lang/String;", cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> cir) {
        if (ConnectHelper.isShared()) {
            cir.setReturnValue(cir.getReturnValue().replace(I18n.format("title.multiplayer.lan"), I18n.format("minetogether.connect.title")));
        }
    }
}
