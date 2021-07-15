package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.module.connect.ConnectHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraftTitle
{
    @Shadow
    @Nullable
    private ServerData currentServer;

    @Inject(at = @At("RETURN"), method = "createTitle()Ljava/lang/String;", cancellable = true)
    private void createTitle(CallbackInfoReturnable<String> cir)
    {
        if (ConnectHelper.isShared() || (currentServer != null && currentServer.ip.contains("minetogether.ch.tools") && currentServer.ip.startsWith("connect")))
        {
            cir.setReturnValue(cir.getReturnValue().replace(I18n.get("title.multiplayer.lan"), I18n.get("minetogether.connect.title")));
        }
    }
}
