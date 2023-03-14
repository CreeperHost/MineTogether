package net.creeperhost.minetogether.mixin.chat;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Created by covers1624 on 26/8/22.
 */
@Mixin (InBedChatScreen.class)
abstract class InBedChatScreenMixin {

    @Redirect (
            method = "keyPressed",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/InBedChatScreen;sendMessage(Ljava/lang/String;)V",
                    opcode = INVOKEVIRTUAL
            )
    )
    private void onSendMessage(InBedChatScreen instance, String s) {
        switch (MineTogetherChat.getTarget()) {
            case VANILLA -> instance.sendMessage(s);
            case PUBLIC -> MineTogetherChat.publicChat.addRecentChat(s);
        }
    }
}
