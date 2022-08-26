package net.creeperhost.minetogether.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by covers1624 on 26/8/22.
 */
@Mixin (ForgeIngameGui.class)
abstract class ForgeIngameGuiMixin extends Gui {

    public ForgeIngameGuiMixin(Minecraft mc) {
        super(mc);
    }

    @Redirect (
            method = "renderChat",
            at = @At (
                    value = "FIELD",
                    target = "Lnet/minecraftforge/client/gui/ForgeIngameGui;chat:Lnet/minecraft/client/gui/components/ChatComponent;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ChatComponent onRender(ForgeIngameGui instance, int width, int height, PoseStack pStack) {
        return getChat();
    }
}
