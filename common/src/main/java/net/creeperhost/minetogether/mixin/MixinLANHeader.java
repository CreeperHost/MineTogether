package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.module.connect.ConnectHelper;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerSelectionList.LANHeader.class)
public class MixinLANHeader {
    private static final Component OURSCANNING_LABEL = new TranslatableComponent("minetogether.connect.scan");
    private static final Component SCANNING_LABEL = new TranslatableComponent("minetogether.connect.scan.offline");
    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIIIIZF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;SCANNING_LABEL:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD))
    private Component render(ServerSelectionList serverSelectionList) {
        return ConnectHelper.isEnabled ?  OURSCANNING_LABEL : SCANNING_LABEL;
    }
}
