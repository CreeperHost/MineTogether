package net.creeperhost.minetogether.mixin.chat;

import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.MessageDropdownOption;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.chat.gui.FriendRequestScreen;
import net.creeperhost.minetogether.chat.ingame.MTChatComponent;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.creeperhost.minetogether.polylib.gui.PreviewRenderer;
import net.creeperhost.minetogether.polylib.gui.RadioButton;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.MalformedURLException;
import java.net.URL;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Created by covers1624 on 5/8/22.
 */
@Mixin (ChatScreen.class)
abstract class ChatScreenMixin extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private RadioButton vanillaChatButton;
    private RadioButton mtChatButton;
    private DropdownButton<MessageDropdownOption> dropdownButton;

    @Nullable
    private Message clickedMessage;

    @Shadow
    protected EditBox input;

    private final PreviewRenderer previewRenderer = new PreviewRenderer(5, 5, 80, 60) {
        @Override
        protected URL getUrlUnderMouse(int mouseX, int mouseY) {
            if (MineTogetherChat.target != ChatTarget.PUBLIC) return null;

            Style style = MineTogetherChat.publicChat.getStyleUnderMouse(mouseX, mouseY);
            if (style == null) return null;
            HoverEvent event = style.getHoverEvent();
            if (event == null || event.getAction() != MessageFormatter.SHOW_URL_PREVIEW) return null;
            Component value = event.getValue(MessageFormatter.SHOW_URL_PREVIEW);

            try {
                return new URL(value.getString());
            } catch (MalformedURLException ex) {
                return null;
            }
        }
    };

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

        dropdownButton = addRenderableWidget(new DropdownButton<>(100, 20, clicked -> {
            assert clickedMessage != null;
            assert clickedMessage.sender != null;
            switch (clicked) {
                case MUTE -> clickedMessage.sender.mute();
                case ADD_FRIEND -> minecraft.setScreen(new FriendRequestScreen(this, clickedMessage.sender, FriendRequestScreen.Type.REQUEST));
                // TODO requires replacing known user names in to-be-sent messages.
                case MENTION -> {
                    String val = input.getValue();
                    if (!val.isEmpty() && val.charAt(val.length() - 1) != ' ') {
                        val = val + " ";
                    }
                    input.setValue(val + clickedMessage.sender.getDisplayName());
                }
                default -> LOGGER.info("Dropdown action not currently implemented! {}", clicked);
            }
        }));
        dropdownButton.setEntries(MessageDropdownOption.VALUES);
        dropdownButton.setFlipped(true);

        vanillaChatButton.linkButtons(mtChatButton);
        mtChatButton.linkButtons(vanillaChatButton);
        switch (MineTogetherChat.target) {
            case VANILLA -> vanillaChatButton.setPressed(true);
            case PUBLIC -> mtChatButton.setPressed(true);
        }

        addRenderableOnly(previewRenderer);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Needs to be done explicitly here, so we prioritize button clicks
        // over message clicks. We can't move super.mouseClicked here as that would
        // let vanilla handle clicked styles before us.
        if (dropdownButton.mouseClicked(mouseX, mouseY, button)) return true;

        if (MineTogetherChat.target == ChatTarget.PUBLIC && tryClickMTChat(MineTogetherChat.publicChat, mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean tryClickMTChat(MTChatComponent mtChat, double mouseX, double mouseY) {
        if (!mtChat.handleClick(mouseX, mouseY)) return false;

        Message message = mtChat.getClickedMessage();
        if (message == null) return false;

        clickedMessage = message;
        mtChat.clearClickedMessage();

        dropdownButton.openAt(mouseX, mouseY);
        return true;
    }

    @Redirect (
            method = "keyPressed",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/ChatScreen;sendMessage(Ljava/lang/String;)V",
                    opcode = INVOKEVIRTUAL
            )
    )
    private void onSendMessage(ChatScreen instance, String s) {
        switch (MineTogetherChat.target) {
            case VANILLA -> instance.sendMessage(s);
            case PUBLIC -> MineTogetherChat.publicChat.addRecentChat(s);
        }
    }
}
