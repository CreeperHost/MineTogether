package net.creeperhost.minetogether.mixin.chat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.*;
import net.creeperhost.minetogether.chat.gui.FriendRequestScreen;
import net.creeperhost.minetogether.chat.ingame.MTChatComponent;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.gui.SettingGui;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.polylib.gui.*;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
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
@Mixin (ChatScreen.class)
abstract class ChatScreenMixin extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private RadioButton vanillaChatButton;
    private RadioButton mtChatButton;
    private SlideButton chatScaleSlider;
    private SlideButton chatWidthSlider;
    private SlideButton chatHeightSlider;
    private IconButton settingsButton;
    private DropdownButton<MessageDropdownOption> dropdownButton;

    @Nullable
    private Message clickedMessage;

    @Shadow
    protected EditBox input;

    @Shadow
    CommandSuggestions commandSuggestions;

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

    @Inject (
            method = "init",
            at = @At ("TAIL")
    )
    private void onInit(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.instance().chatEnabled || mc.options.hideGui) return;

        ChatComponent chat = mc.gui.getChat();
        float cScale = (float) chat.getScale();
        int cWidth = Mth.ceil((float) chat.getWidth() + (12 * cScale));
        int cHeight = Mth.ceil(chat.getHeight() * cScale);

        vanillaChatButton = addRenderableWidget(new RadioButton(0, 0, 12, 100, mc.isLocalServer() ? Component.translatable("minetogether:ingame.chat.local") : Component.translatable("minetogether:ingame.chat.server")))
                .withAutoScaleText(3)
                .withVerticalText()
                .selectedSupplier(() -> MineTogetherChat.getTarget() == ChatTarget.VANILLA)
                .onPressed(e -> MineTogetherChat.setTarget(ChatTarget.VANILLA))
                .onRelease(() -> setFocused(input));

        mtChatButton = addRenderableWidget(new RadioButton(0, 0, 12, 100, Component.translatable("minetogether:ingame.chat.global")))
                .withAutoScaleText(3)
                .withVerticalText()
                .selectedSupplier(() -> MineTogetherChat.getTarget() == ChatTarget.PUBLIC)
                .onPressed(e -> MineTogetherChat.setTarget(ChatTarget.PUBLIC))
                .onRelease(() -> setFocused(input));

        settingsButton = addRenderableWidget(new IconButton(0, 0, 12, 12, new ResourceLocation(MineTogether.MOD_ID, "textures/gui/buttons/gear.png"), e -> mc.setScreen(new ModularGuiScreen(new SettingGui(), mc.screen))));

        chatScaleSlider = addRenderableWidget(new SlideButton(0, 0, 12, 200))
                .setDynamicMessage(() -> Component.translatable("options.percent_value", Component.translatable("options.chat.scale"), (int) (mc.options.chatScale().get() * 100.0)))
                .bindValue(() -> mc.options.chatScale().get(), value -> {
                    mc.options.chatScale().set(value);
                    updateButtons();
                })
                .setRange(0.25, 1)
                .withTextScale(0.75F)
                .onRelease(() -> setFocused(input))
                .setEnabled(() -> commandSuggestions.suggestions == null && Config.instance().chatSettingsSliders)
                .withAutoScaleText(3);

        chatWidthSlider = addRenderableWidget(new SlideButton(0, 0, 12, 200))
                .setDynamicMessage((newValue) -> Component.translatable("options.pixel_value", Component.translatable("options.chat.width"), ChatComponent.getWidth(newValue)))
                .bindValue(() -> mc.options.chatWidth().get(), value -> {
                    mc.options.chatWidth().set(value);
                    updateButtons();
                })
                .withTextScale(0.75F)
                .onRelease(() -> setFocused(input))
                .setApplyOnRelease(true)
                .setEnabled(() -> commandSuggestions.suggestions == null && Config.instance().chatSettingsSliders)
                .withAutoScaleText(3);

        chatHeightSlider = addRenderableWidget(new SlideButton(0, 0, 12, 200))
                .setDynamicMessage(() -> Component.translatable("options.pixel_value", Component.translatable("options.chat.height.focused"), ChatComponent.getHeight(mc.options.chatHeightFocused().get())))
                .bindValue(() -> mc.options.chatHeightFocused().get(), value -> {
                    mc.options.chatHeightFocused().set(value);
                    updateButtons();
                })
                .withTextScale(0.75F)
                .onRelease(() -> setFocused(input))
                .setEnabled(() -> commandSuggestions.suggestions == null && Config.instance().chatSettingsSliders)
                .withAutoScaleText(3);

        updateButtons();

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

        addRenderableOnly(previewRenderer);

        newUserButton = addWidget(new Button(6, height - ((cHeight + 80) / 2) + 45, cWidth - 2, 20, Component.literal("Join " + ChatStatistics.onlineCount + " online users now!"), e -> {
            MineTogetherChat.setNewUserResponded();
        }));
        disableButton = addWidget(new Button(6, height - ((cHeight + 80) / 2) + 70, cWidth - 2, 20, Component.literal("Don't ask me again."), e -> {
            MineTogetherChat.disableChat();
            Config.instance().chatEnabled = false;
            Config.save();
            MineTogetherChat.setNewUserResponded();
            clearWidgets();
        }));
        newUserButton.visible = false;
        disableButton.visible = false;

        if (MineTogetherChat.isNewUser() && MineTogetherChat.getTarget() == ChatTarget.PUBLIC) {
            ChatStatistics.pollStats();
            newUserButton.visible = true;
            disableButton.visible = true;
        }

        switchToVanillaIfCommand();
    }

    private void updateButtons() {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.instance().chatEnabled || mc.options.hideGui) {
            return;
        }
        ChatComponent chat = mc.gui.getChat();
        float cScale = (float) chat.getScale();
        int cWidth = Mth.ceil((float) chat.getWidth() + (12 * cScale)); //Vanilla does some wired s%$#. This mostly accounts for it.
        int cHeight = Mth.ceil(chat.getHeight() * cScale) - 12;
        int guiHeight = height;
        int cMaxYPos = guiHeight - 40;

        int vPos = cMaxYPos - cHeight - 12;
        int vHeight = cHeight / 2;
        int mtPos = vPos + vHeight;
        int mtHeight = cMaxYPos - mtPos - 12;

        vanillaChatButton.updateBounds(cWidth, vPos, 12, vHeight);
        mtChatButton.updateBounds(cWidth, mtPos, 12, mtHeight);
        settingsButton.updateBounds(cWidth, mtPos + mtHeight, 12, 12);

        int sliderWidth = cWidth / 3;
        chatWidthSlider.updateBounds(0, cMaxYPos + 2, sliderWidth, 10);
        chatHeightSlider.updateBounds(sliderWidth + 2, cMaxYPos + 2, sliderWidth, 10);
        chatScaleSlider.updateBounds((sliderWidth * 2) + 4, cMaxYPos + 2, cWidth - (sliderWidth * 2) - 4, 10);
    }

    @Inject(
            method = "mouseClicked",
            at = @At ("HEAD"),
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

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.instance().chatEnabled || mc.options.hideGui) {
            return super.mouseReleased(d, e, i);
        }

        //Needed because vanilla does not bother to send release if the mouse is not over the component.
        chatWidthSlider.mouseReleased(d, e, i);
        chatHeightSlider.mouseReleased(d, e, i);
        chatScaleSlider.mouseReleased(d, e, i);

        //Ensure input box is always focused after input.
        setFocused(input);
        return super.mouseReleased(d, e, i);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (!Config.instance().chatEnabled || minecraft.options.hideGui) {
            return super.keyReleased(i, j, k);
        }
        //Prevent focus from being directed away from text box via arrow keys
        setFocused(input);
        return super.keyReleased(i, j, k);
    }


    @Override
    public void mouseMoved(double d, double e) {
        if (!Config.instance().chatEnabled || minecraft.options.hideGui) {
            super.mouseMoved(d, e);
            return;
        }
        chatWidthSlider.mouseMove(d, e);
        chatHeightSlider.mouseMove(d, e);
        chatScaleSlider.mouseMove(d, e);
        super.mouseMoved(d, e);
    }

    @Inject(
            method = "render",
            at = @At ("TAIL")
    )
    private void onRender(PoseStack pStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (MineTogetherChat.getTarget() == ChatTarget.PUBLIC && MineTogetherChat.isNewUser()) {
            pStack.pushPose();
            pStack.translate(0, 0, 100); // Push it forward a little bit so It's actually above the text.

            ChatComponent chatComponent = MineTogetherChat.publicChat;
            int y = height - 43 - (minecraft.font.lineHeight * Math.max(Math.min(chatComponent.getRecentChat().size(), chatComponent.getLinesPerPage()), 20));
            fill(pStack, 0, y, chatComponent.getWidth() + 6, chatComponent.getHeight() + 10 + y, 0x99000000);

            drawCenteredString(pStack, font, Component.translatable("minetogether:new_user.1"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2), 0xFFFFFF);
            drawCenteredString(pStack, font, Component.translatable("minetogether:new_user.2"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 10, 0xFFFFFF);
            drawCenteredString(pStack, font, Component.translatable("minetogether:new_user.3"), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 20, 0xFFFFFF);
            drawCenteredString(pStack, font, Component.translatable("minetogether:new_user.4", ChatStatistics.userCount), (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 30, 0xFFFFFF);

            // Render these manually after the grey-out, so they are on top of it.
            newUserButton.render(pStack, mouseX, mouseY, partialTicks);
            disableButton.render(pStack, mouseX, mouseY, partialTicks);
            pStack.popPose();
        }
    }

    @Inject(
            method = "tick",
            at = @At ("TAIL")
    )
    public void tick(CallbackInfo ci) {
        switchToVanillaIfCommand();

        // If we are the vanilla chat, set things editable, and bail out.
        if (MineTogetherChat.getTarget() == ChatTarget.VANILLA) {
            input.setEditable(true);
            input.setSuggestion("");
            return;
        }

        if (MineTogetherChat.isNewUser()) {
            newUserButton.visible = true;
            disableButton.visible = true;
            input.setFocus(false);
            input.setEditable(false);
            return;
        }

        IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
        if (state != IrcState.CONNECTED) {
            input.setFocus(false);
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

    @Inject(
            method = "sendsChatPreviewRequests",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendsChatPreviewRequests(CallbackInfoReturnable<Boolean> cir) {
        if (MineTogetherChat.getTarget() != ChatTarget.VANILLA) {
            cir.setReturnValue(false);
        }
    }

    private boolean switchToVanillaIfCommand() {
        if (MineTogetherChat.getTarget() == ChatTarget.VANILLA)  return false;
        if (!input.getValue().startsWith("/")) return false;

        MineTogetherChat.setTarget(ChatTarget.VANILLA);
        return true;
    }
}
