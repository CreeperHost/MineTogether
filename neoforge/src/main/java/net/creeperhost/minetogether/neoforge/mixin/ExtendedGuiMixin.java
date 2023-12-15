package net.creeperhost.minetogether.neoforge.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by covers1624 on 26/8/22.
 */
@Mixin (ExtendedGui.class)
abstract class ExtendedGuiMixin extends Gui {

    public ExtendedGuiMixin(Minecraft mc) {
        super(mc, mc.getItemRenderer());
    }

    @Redirect (
            method = "renderChat",
            at = @At (
                    value = "FIELD",
                    target = "Lnet/neoforged/neoforge/client/gui/overlay/ExtendedGui;chat:Lnet/minecraft/client/gui/components/ChatComponent;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ChatComponent onRender(ExtendedGui instance, int width, int height, GuiGraphics pStack) {
        return getChat();
    }
}
