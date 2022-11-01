package net.creeperhost.minetogether.mixin.chat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.*;
import net.creeperhost.minetogether.chat.gui.FriendRequestScreen;
import net.creeperhost.minetogether.chat.ingame.MTChatComponent;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.creeperhost.minetogether.polylib.gui.PreviewRenderer;
import net.creeperhost.minetogether.polylib.gui.RadioButton;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        if (!Config.instance().chatEnabled) return;

        int cWidth = Minecraft.getInstance().gui.getChat().getWidth();
        int cHeight = Minecraft.getInstance().gui.getChat().getHeight();
        int x = Mth.ceil(((float) cWidth)) + 6;

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

        if (MineTogetherChat.isNewUser() && MineTogetherChat.target == ChatTarget.PUBLIC) {
            ChatStatistics.pollStats();

            addRenderableWidget(new Button(6, height - ((cHeight + 80) / 2) + 45, cWidth - 2, 20, new TextComponent("Join " + ChatStatistics.onlineCount + " online users now!"), e -> {
                MineTogetherChat.setNewUserResponded();
                minecraft.setScreen(null);
            }));
            addRenderableWidget(new Button(6, height - ((cHeight + 80) / 2) + 70, cWidth - 2, 20, new TextComponent("Don't ask me again."), e -> {
                MineTogetherChat.disableChat();
                MineTogetherChat.setNewUserResponded();
                clearWidgets();
            }));
        }

        switchToVanillaIfCommand();
    }

    @Inject(
            method = "mouseClicked",
            at = @At ("HEAD"),
            cancellable = true
    )
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // Needs to be done explicitly here, so we prioritize button clicks
        // over message clicks. We can't move super.mouseClicked here as that would
        // let vanilla handle clicked styles before us.
        if (dropdownButton.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }

        if (MineTogetherChat.target == ChatTarget.PUBLIC && tryClickMTChat(MineTogetherChat.publicChat, mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "render",
            at = @At ("TAIL")
    )
    private void onRender(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        if (MineTogetherChat.target == ChatTarget.PUBLIC && MineTogetherChat.isNewUser()) {
            ChatComponent chatComponent = MineTogetherChat.publicChat;
            int y = height - 43 - (minecraft.font.lineHeight * Math.max(Math.min(chatComponent.getRecentChat().size(), chatComponent.getLinesPerPage()), 20));
            fill(poseStack, 0, y, chatComponent.getWidth() + 6, chatComponent.getHeight() + 10 + y, 0x99000000);

            drawCenteredString(poseStack, font, new TranslatableComponent("minetogether:new_user.1"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2), 0xFFFFFF);
            drawCenteredString(poseStack, font, new TranslatableComponent("minetogether:new_user.2"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 10, 0xFFFFFF);
            drawCenteredString(poseStack, font, new TranslatableComponent("minetogether:new_user.3"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 20, 0xFFFFFF);
            drawCenteredString(poseStack, font, new TranslatableComponent("minetogether:new_user.4", ChatStatistics.userCount), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 30, 0xFFFFFF);
        }
    }

    @Inject(
            method = "tick",
            at = @At ("TAIL")
    )
    public void tick(CallbackInfo ci) {
        if (switchToVanillaIfCommand()) return;

        IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
        if (state == IrcState.CONNECTED || MineTogetherChat.target == ChatTarget.VANILLA) {
            input.setEditable(true);
            input.setSuggestion("");
            return;
        }

        input.setFocus(false);
        input.setEditable(false);
        input.setSuggestion(new TranslatableComponent(ChatConstants.STATE_SUGGESTION_LOOKUP.get(state)).getString());
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

    private boolean switchToVanillaIfCommand() {
        if (MineTogetherChat.target == ChatTarget.VANILLA)  return false;
        if (!input.getValue().startsWith("/")) return false;

        MineTogetherChat.target = ChatTarget.VANILLA;
        vanillaChatButton.selectButton();
        return true;
    }
}
