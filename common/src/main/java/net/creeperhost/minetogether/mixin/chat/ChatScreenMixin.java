package net.creeperhost.minetogether.mixin.chat;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by covers1624 on 5/8/22.
 */
@Mixin(ChatScreen.class)
abstract class ChatScreenMixin extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private RadioButton vanillaChatButton;
    private RadioButton mtChatButton;
    private DropdownButton<MessageDropdownOption> dropdownButton;

    @Nullable
    private Message clickedMessage;

    @Shadow
    protected EditBox input;

    private Button newUserButton;
    private Button disableButton;

    private final PreviewRenderer previewRenderer = new PreviewRenderer(5, 5, 80, 60) {
        @Override
        protected URL getUrlUnderMouse(int mouseX, int mouseY) {
            if (MineTogetherChat.getTarget() != ChatTarget.PUBLIC) return null;

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

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void onInit(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.instance().chatEnabled || mc.options.hideGui) return;

        int cWidth = mc.gui.getChat().getWidth();
        int cHeight = mc.gui.getChat().getHeight();
        int x = Mth.ceil(((float) cWidth)) + 6;

        vanillaChatButton = new RadioButton(x, height - 215, 12, 87, Component.translatable("minetogether:ingame.chat.local"))
                .withTextScale(0.75F)
                .withVerticalText()
                .onPressed(e -> MineTogetherChat.setTarget(ChatTarget.VANILLA));
        mtChatButton = new RadioButton(x, height - 215 + 87, 12, 87, Component.translatable("minetogether:ingame.chat.global"))
                .withTextScale(0.75F)
                .withVerticalText()
                .onPressed(e -> MineTogetherChat.setTarget(ChatTarget.PUBLIC));
        addRenderableWidget(vanillaChatButton);
        addRenderableWidget(mtChatButton);

        dropdownButton = addRenderableWidget(new DropdownButton<>(100, 20, clicked -> {
            assert clickedMessage != null;
            assert clickedMessage.sender != null;
            switch (clicked) {
                case MUTE -> clickedMessage.sender.mute();
                case ADD_FRIEND ->
                        minecraft.setScreen(new FriendRequestScreen(this, clickedMessage.sender, FriendRequestScreen.Type.REQUEST));
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
        switch (MineTogetherChat.getTarget()) {
            case VANILLA -> vanillaChatButton.setPressed(true);
            case PUBLIC -> mtChatButton.setPressed(true);
        }

        addRenderableOnly(previewRenderer);

        newUserButton = addWidget(Button.builder(Component.literal("Join " + ChatStatistics.onlineCount + " online users now!"), e -> {
                            MineTogetherChat.setNewUserResponded();
                        })
                        .bounds(6, height - ((cHeight + 80) / 2) + 45, cWidth - 2, 20)
                        .build()
        );
        disableButton = addWidget(Button.builder(Component.literal("Don't ask me again."), e -> {
                            MineTogetherChat.disableChat();
                            Config.instance().chatEnabled = false;
                            Config.save();
                            MineTogetherChat.setNewUserResponded();
                            clearWidgets();
                        })
                        .bounds(6, height - ((cHeight + 80) / 2) + 70, cWidth - 2, 20)
                        .build()
        );
        newUserButton.visible = false;
        disableButton.visible = false;

        if (MineTogetherChat.isNewUser() && MineTogetherChat.getTarget() == ChatTarget.PUBLIC) {
            ChatStatistics.pollStats();
            newUserButton.visible = true;
            disableButton.visible = true;
        }

        switchToVanillaIfCommand();
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.instance().chatEnabled || Minecraft.getInstance().options.hideGui) return;

        // Needs to be done explicitly here, so we prioritize button clicks
        // over message clicks. We can't move super.mouseClicked here as that would
        // let vanilla handle clicked styles before us.
        if (dropdownButton.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }

        if (MineTogetherChat.getTarget() == ChatTarget.PUBLIC && tryClickMTChat(MineTogetherChat.publicChat, mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (MineTogetherChat.getTarget() == ChatTarget.PUBLIC && MineTogetherChat.isNewUser()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 100); // Push it forward a little bit so It's actually above the text.

            ChatComponent chatComponent = MineTogetherChat.publicChat;
            int y = height - 43 - (minecraft.font.lineHeight * Math.max(Math.min(chatComponent.getRecentChat().size(), chatComponent.getLinesPerPage()), 20));
            graphics.fill(0, y, chatComponent.getWidth() + 6, chatComponent.getHeight() + 10 + y, 0x99000000);

            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.1"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2), 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.2"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 10, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.3"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 20, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.4", ChatStatistics.userCount), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 30, 0xFFFFFF);

            // Render these manually after the grey-out, so they are on top of it.
            newUserButton.render(graphics, mouseX, mouseY, partialTicks);
            disableButton.render(graphics, mouseX, mouseY, partialTicks);
            graphics.pose().popPose();
        }
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    public void tick(CallbackInfo ci) {
        switchToVanillaIfCommand();
        setFocused(input);

        // If we are the vanilla chat, set things editable, and bail out.
        if (MineTogetherChat.getTarget() == ChatTarget.VANILLA) {
            input.setEditable(true);
            input.setSuggestion("");
            return;
        }

        if (MineTogetherChat.isNewUser()) {
            newUserButton.visible = true;
            disableButton.visible = true;
            setFocused(null);
            input.setEditable(false);
            return;
        }

        IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
        if (state != IrcState.CONNECTED) {
            setFocused(null);
            input.setEditable(false);
            input.setSuggestion(Component.translatable(ChatConstants.STATE_SUGGESTION_LOOKUP.get(state)).getString());
            return;
        }

        input.setEditable(true);
        input.setSuggestion("");
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

    @Inject(
            method = "handleChatInput",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHandleChatInput(String message, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (MineTogetherChat.getTarget() == ChatTarget.PUBLIC) {
            MineTogetherChat.publicChat.addRecentChat(message);
            cir.setReturnValue(true);
        }
    }

    //TODO looks like the code this references has been completely removed. Not sure if this is going to be an issue.
//    @Inject(
//            method = "sendsChatPreviewRequests",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void onSendsChatPreviewRequests(CallbackInfoReturnable<Boolean> cir) {
//        if (MineTogetherChat.getTarget() != ChatTarget.VANILLA) {
//            cir.setReturnValue(false);
//        }
//    }

    private boolean switchToVanillaIfCommand() {
        if (MineTogetherChat.getTarget() == ChatTarget.VANILLA) return false;
        if (!input.getValue().startsWith("/")) return false;

        MineTogetherChat.setTarget(ChatTarget.VANILLA);
        vanillaChatButton.selectButton();
        return true;
    }
}
