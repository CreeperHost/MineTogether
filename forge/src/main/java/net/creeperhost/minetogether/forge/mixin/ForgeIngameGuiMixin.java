package net.creeperhost.minetogether.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by covers1624 on 26/8/22.
 */
@Mixin (ForgeGui.class)
abstract class ForgeIngameGuiMixin extends Gui {

    public ForgeIngameGuiMixin(Minecraft mc) {
        super(mc, mc.getItemRenderer());
    }

    @Redirect (
            method = "renderChat",
            at = @At (
                    value = "FIELD",
                    target = "Lnet/minecraftforge/client/gui/overlay/ForgeGui;chat:Lnet/minecraft/client/gui/components/ChatComponent;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ChatComponent onRender(ForgeGui instance, int width, int height, PoseStack pStack) {
        return getChat();
    }
}
