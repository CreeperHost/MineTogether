package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.minetogether.chat.MessageDropdownOption;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.gui.dialogs.ContextMenu;
import net.creeperhost.minetogether.gui.dialogs.TextInputDialog;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by brandon3055 on 01/07/2024
 */
public class ChatScreenInjection implements GuiProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private ModularGui gui;

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return new PaddedRoot(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        this.gui = gui;
        gui.initFullscreenGui();
    }

    public boolean canShowDialog() {
        return gui.getRoot().getChildren().isEmpty();
    }

    public void openMessageDialog(Message message, EditBox input, double mouseX, double mouseY) {
        ContextMenu menu = new ContextMenu(gui.getRoot());
        menu.addTitle(Component.literal(message.senderName.getMessage()).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD));
        for (MessageDropdownOption value : MessageDropdownOption.VALUES) {
            switch (value) {
                case MUTE -> menu.addOption(value.getTitle(true).copy().withStyle(ChatFormatting.RED), message.sender::mute);
                case ADD_FRIEND -> menu.addOption(value.getTitle(true).copy().withStyle(ChatFormatting.AQUA), () -> {
//                    gui.mc().setScreen(new FriendRequestScreen(gui.getScreen(), message.sender, FriendRequestScreen.Type.REQUEST));
                    ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
                    new TextInputDialog(gui.getRoot(), Component.translatable("minetogether:screen.friendreq.desc.request"), MessageElement.displayName(message.sender))
                            .setResultCallback(friendName -> {
                                Profile target = message.sender;
                                if (!target.hasFriendCode()) {
                                    //TODO, Is this actually something we will run into? Do we need to add handling for this?
                                    LOGGER.warn("User profile incomplete, unable to send friend request at this time.");
                                    MineTogetherChat.simpleToast(Component.literal("Error, Profile Incomplete").withStyle(ChatFormatting.RED));
                                    return;
                                }
                                profileManager.sendFriendRequest(target.getFriendCode(), friendName.trim(), success -> {
                                    MineTogetherChat.simpleToast(Component.translatable(success ? "minetogether:gui.friends.request_sent" : "minetogether:gui.friends.request_fail"));
                                });
                            });
                });
                case MENTION -> menu.addOption(value.getTitle(true).copy().withStyle(ChatFormatting.AQUA), () -> {
                    String val = input.getValue();
                    if (!val.isEmpty() && val.charAt(val.length() - 1) != ' ') {
                        val = val + " ";
                    }
                    input.setValue(val + message.sender.getDisplayName());
                });
            }
        }
        menu.setPosition(mouseX, mouseY);
    }

    //Parts of the chat screen are offset by as much as 100 in the z direction, So we use a custom root element that applies some padding under our elements.
    private static class PaddedRoot extends GuiElement<PaddedRoot> implements BackgroundRender{

        public PaddedRoot(@NotNull GuiParent<?> parent) {
            super(parent);
        }

        @Override
        public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {}

        @Override
        public double getBackgroundDepth() {
            return 105;
        }
    }
}
