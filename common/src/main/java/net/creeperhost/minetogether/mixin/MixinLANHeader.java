package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.module.connect.ConnectHelper;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.class)
public class MixinLANHeader
{
    private static final Component OURSCANNING_LABEL = new TranslatableComponent("minetogether.connect.scan");
    private static final Component MTSCANNING_LABEL = new TranslatableComponent("minetogether.connect.scan.offline");

    @Mutable
    @Shadow @Final private static Component SCANNING_LABEL;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(CallbackInfo ci)
    {
       SCANNING_LABEL = ConnectHelper.isEnabled ? OURSCANNING_LABEL : MTSCANNING_LABEL;
    }
}
