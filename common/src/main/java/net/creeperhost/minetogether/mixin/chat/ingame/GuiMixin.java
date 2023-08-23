package net.creeperhost.minetogether.mixin.chat.ingame;

import net.covers1624.quack.util.SneakyUtils;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by covers1624 on 27/7/22.
 */
@Mixin (Gui.class)
abstract class GuiMixin {

    @Final
    @Shadow
    public ChatComponent chat;

    @Inject (
            method = "<init>",
            at = @At (
                    value = "TAIL"
            )
    )
    private void onInit(Minecraft minecraft, CallbackInfo ci) {
        MineTogetherChat.initChat(SneakyUtils.unsafeCast(this));
    }

    @Redirect (
            method = "getChat",
            at = @At (
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/Gui;chat:Lnet/minecraft/client/gui/components/ChatComponent;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ChatComponent onGetChat(Gui instance) {
        switch (MineTogetherChat.getTarget()) {
            case VANILLA:
                return chat;
            case PUBLIC:
                return MineTogetherChat.publicChat;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Redirect (
            method = "render",
            at = @At (
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/Gui;chat:Lnet/minecraft/client/gui/components/ChatComponent;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ChatComponent onRender(Gui instance) {
        switch (MineTogetherChat.getTarget()) {
            case VANILLA:
                return chat;
            case PUBLIC:
                return MineTogetherChat.publicChat;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Inject(
            method = "tick()V",
            at = @At("TAIL")
    )
    private void onTick(CallbackInfo ci) {
        MineTogetherChat.publicChat.tick();
    }
}
