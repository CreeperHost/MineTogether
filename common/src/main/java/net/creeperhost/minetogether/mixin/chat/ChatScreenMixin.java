package net.creeperhost.minetogether.mixin.chat;

import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.polylib.gui.RadioButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by covers1624 on 5/8/22.
 */
@Mixin (ChatScreen.class)
abstract class ChatScreenMixin extends Screen {

    private RadioButton vanillaChatButton;
    private RadioButton mtChatButton;

    protected ChatScreenMixin(Component component) {
        super(component);
    }

    @Inject (
            method = "init",
            at = @At ("TAIL")
    )
    private void onInit(CallbackInfo ci) {
        int x = Mth.ceil(((float) Minecraft.getInstance().gui.getChat().getWidth())) + 6;

        vanillaChatButton = new RadioButton(x, height - 215, 12, 87, new TranslatableComponent("minetogether:ingame.chat.local"))
                .withTextScale(0.75F)
                .withVerticalText()
                .onPressed(e -> MineTogetherChat.target = ChatTarget.VANILLA);
        mtChatButton = new RadioButton(x, height - 215 + 87, 12, 87, new TranslatableComponent("minetogether:ingame.chat.global"))
                .withTextScale(0.75F)
                .withVerticalText()
                .onPressed(e -> MineTogetherChat.target = ChatTarget.PUBLIC);
        addRenderableWidget(vanillaChatButton);
        addRenderableWidget(mtChatButton);

        vanillaChatButton.linkButtons(mtChatButton);
        mtChatButton.linkButtons(vanillaChatButton);
        switch (MineTogetherChat.target) {
            case VANILLA -> vanillaChatButton.setPressed(true);
            case PUBLIC -> mtChatButton.setPressed(true);
        }
    }
}
