package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.ChatConstants;
import net.creeperhost.minetogether.chat.ChatStatistics;
import net.creeperhost.minetogether.chat.MessageDropdownOption;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.gui.SettingsScreen;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.creeperhost.minetogether.polylib.gui.IconButton;
import net.creeperhost.minetogether.polylib.gui.StringButton;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * @author covers1624
 */
public class ChatScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Screen parent;
    @Nullable
    private IrcChannel channel;

    @Nullable
    private ChatScrollList chatList;
    @Nullable
    private EditBox sendEditBox;

    @Nullable
    private StringButton connectionStatus;

    private Button friendsList;

    private DropdownButton<MessageDropdownOption> messageDropdownButton;

    @Nullable
    private Message clickedMessage;

    private Button newUserButton;
    private Button disableButton;
    private boolean newUser = MineTogetherChat.isNewUser();

    public ChatScreen(Screen parent) {
        super(Component.translatable("minetogether:screen.chat.title"));
        this.parent = parent;
    }

    public void attach(IrcChannel channel) {
        this.channel = channel;
        if (chatList != null) {
            chatList.attach(channel);
        }
    }

    @Override
    protected void init() {
        assert minecraft != null;

        chatList = new ChatScrollList(minecraft, width - 20, height - 50, 30, height - 50);
        chatList.setLeftPos(10);
        chatList.setScrollAmount(chatList.getMaxScroll());

        channel = MineTogetherChat.CHAT_STATE.ircClient.getPrimaryChannel();
        if (channel != null) {
            attach(channel);
        }
        boolean shouldFocusEditBox = sendEditBox == null || sendEditBox.isFocused();
        sendEditBox = new EditBox(minecraft.font, 11, height - 48, width - 22, 20, sendEditBox, Component.empty());
        sendEditBox.setFocused(shouldFocusEditBox);
        sendEditBox.setMaxLength(256);

        addRenderableWidget(chatList);
        addRenderableWidget(sendEditBox);

        addRenderableWidget(new IconButton(width - 124, 5, 3, Constants.WIDGETS_SHEET, e -> {
            minecraft.setScreen(new SettingsScreen(this));
        }));
        addRenderableWidget(Button.builder(Component.translatable("minetogether:button.cancel"), button -> minecraft.setScreen(parent))
                .bounds(width - 100 - 5, height - 5 - 20, 100, 20)
                .build()
        );

        addRenderableWidget(connectionStatus = new StringButton(8, height - 20, 70, 20, false, () -> {
            IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
            return Component.literal(ChatConstants.STATE_FORMAT_LOOKUP.get(state) + "\u2022" + " " + ChatFormatting.WHITE + ChatConstants.STATE_DESC_LOOKUP.get(state));
        }, button -> {
            if (MineTogetherChat.CHAT_STATE.ircClient.getState() == IrcState.BANNED) {
                minecraft.setScreen(new ConfirmScreen(t -> {
                    if (t) {
                        Util.getPlatform().openUri("https://minetogether.io/profile/standing");
                    }
                    minecraft.setScreen(this);
                }, Component.translatable("minetogether:screen.banned.line1"), Component.translatable("minetogether:screen.banned.line2")));
            }
        }));

        addRenderableWidget(friendsList = Button.builder(Component.translatable("minetogether:button.friends"), e -> minecraft.setScreen(new FriendsListScreen(this)))
                .bounds(5, 5, 100, 20)
                .build()
        );

        messageDropdownButton = addRenderableWidget(new DropdownButton<>(100, 20, clicked -> {
            assert clickedMessage != null;
            assert clickedMessage.sender != null;
            switch (clicked) {
                case MUTE -> clickedMessage.sender.mute();
                case ADD_FRIEND ->
                        minecraft.setScreen(new FriendRequestScreen(this, clickedMessage.sender, FriendRequestScreen.Type.REQUEST));
                case MENTION -> {
                    String val = sendEditBox.getValue();
                    if (!val.isEmpty() && val.charAt(val.length() - 1) != ' ') {
                        val = val + " ";
                    }
                    sendEditBox.setValue(val + clickedMessage.sender.getDisplayName());
                }
                default -> LOGGER.info("Dropdown action not currently implemented! {}", clicked);
            }
        }));
        messageDropdownButton.setEntries(MessageDropdownOption.VALUES);

        if (newUser) {
            ChatStatistics.pollStats();
            newUserButton = addWidget(Button.builder(Component.literal("Join " + ChatStatistics.onlineCount + " online users now!"), e -> {
                                MineTogetherChat.setNewUserResponded();
                                minecraft.setScreen(new ChatScreen(parent));
                            })
                            .bounds(width / 2 - 150, 75 + (height / 4), 300, 20)
                            .build()
            );
            disableButton = addWidget(Button.builder(Component.literal("Don't ask me again."), e -> {
                                MineTogetherChat.disableChat();
                                Config.instance().chatEnabled = false;
                                Config.save();
                                MineTogetherChat.setNewUserResponded();
                                minecraft.setScreen(parent);
                            })
                            .bounds(width / 2 - 150, 95 + (height / 4), 300, 20)
                            .build()
            );
        }
    }

    @Override
    protected void clearWidgets() {
        if (chatList != null) {
            chatList.removed();
        }
        super.clearWidgets();
    }

    @Override
    public void removed() {
        if (chatList != null) {
            chatList.removed();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (connectionStatus != null) {
            connectionStatus.tick();
        }

        if (newUser) {
            sendEditBox.setFocused(false);
            sendEditBox.setEditable(false);
            sendEditBox.setSuggestion("");
            return;
        }

        IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
        if (state != IrcState.CONNECTED) {
            sendEditBox.setFocused(false);
            sendEditBox.setEditable(false);
            sendEditBox.setSuggestion(Component.translatable(ChatConstants.STATE_SUGGESTION_LOOKUP.get(state)).getString());
            return;
        }

        sendEditBox.setEditable(true);
        sendEditBox.setSuggestion("");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(font, getTitle(), width / 2, 5, 0xFFFFFF);
        if (newUser) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 100); // Push it forward a little bit so It's actually above the text.

            graphics.fill(10, chatList.getTop(), width - 10, chatList.getHeight(), 0x99000000);
            graphics.fill(10, chatList.getTop(), width - 10, chatList.getHeight(), 0x99000000);

            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.1"), width / 2, (height / 4) + 25, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.2"), width / 2, (height / 4) + 35, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.3"), width / 2, (height / 4) + 45, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("minetogether:new_user.4", ChatStatistics.userCount), width / 2, (height / 4) + 55, 0xFFFFFF);

            // Render these manually after the grey-out, so they are on top of it.
            newUserButton.render(graphics, mouseX, mouseY, partialTicks);
            disableButton.render(graphics, mouseX, mouseY, partialTicks);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        assert sendEditBox != null;

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            String trimmedMessage = sendEditBox.getValue().trim();
            if (!trimmedMessage.isEmpty()) {
                sendEditBox.setValue("");
                if (channel != null) {
                    channel.sendMessage(trimmedMessage);
                }
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean b = super.mouseClicked(mouseX, mouseY, button);
        if (b) return true;

        if (!chatList.isMouseOver(mouseX, mouseY)) return false;
        // No component clicks when new user.
        if (newUser) return false;

        ChatScrollList.ChatLine line = chatList.getEntry(mouseX, mouseY);
        if (line == null) return false;
        if (line.message.sender == null) return false;
        if (line.message.sender == MineTogetherChat.getOurProfile()) return false;

        Style style = minecraft.font.getSplitter().componentStyleAtWidth(line.formattedMessage, (int) mouseX);
        if (style == null || style.getClickEvent() == null) return false;

        ClickEvent event = style.getClickEvent();
        if (event.getValue().equals(MessageFormatter.CLICK_NAME)) {
            clickedMessage = line.message;
            messageDropdownButton.openAt(mouseX, mouseY);
            return true;
        }

        return false;
    }
}
