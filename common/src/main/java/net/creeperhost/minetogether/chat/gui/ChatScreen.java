package net.creeperhost.minetogether.chat.gui;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.MessageDropdownOption;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.creeperhost.minetogether.polylib.gui.StringButton;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * @author covers1624
 */
public class ChatScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<IrcState, ChatFormatting> STATE_FORMAT_LOOKUP = ImmutableMap.of(
            IrcState.DISCONNECTED, ChatFormatting.RED,
            IrcState.CONNECTING, ChatFormatting.GOLD,
            IrcState.RECONNECTING, ChatFormatting.GOLD,
            IrcState.CONNECTED, ChatFormatting.GREEN,
            IrcState.CRASHED, ChatFormatting.RED
    );
    private static final Map<IrcState, String> STATE_DESC_LOOKUP = ImmutableMap.of(
            IrcState.DISCONNECTED, "Disconnected",
            IrcState.CONNECTING, "Connecting",
            IrcState.RECONNECTING, "Reconnecting",
            IrcState.CONNECTED, "Connected",
            IrcState.CRASHED, "Engine crashed"
    );

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

    public ChatScreen(Screen parent) {
        super(new TranslatableComponent("minetogether:screen.chat.title"));
        this.parent = parent;
    }

    public void attach(IrcChannel channel) {
        this.channel = channel;
        chatList.attach(channel);
    }

    @Override
    protected void init() {
        assert minecraft != null;

        chatList = new ChatScrollList(minecraft, width - 20, height - 50, 30, height - 50);
        chatList.setLeftPos(10);
        chatList.setScrollAmount(chatList.getMaxScroll());
        channel = MineTogetherChat.CHAT_STATE.getIrcClient().getPrimaryChannel();
        if (channel != null) {
            attach(channel);
        }
        boolean shouldFocusEditBox = sendEditBox == null || sendEditBox.isFocused();
        sendEditBox = new EditBox(minecraft.font, 11, height - 48, width - 22, 20, sendEditBox, TextComponent.EMPTY);
        sendEditBox.setFocus(shouldFocusEditBox);
        sendEditBox.setMaxLength(256);

        addRenderableWidget(chatList);
        addRenderableWidget(sendEditBox);

        addRenderableWidget(new Button(width - 100 - 5, height - 5 - 20, 100, 20, new TranslatableComponent("minetogether:button.cancel"), button -> {
            minecraft.setScreen(parent);
        }));

        addRenderableWidget(connectionStatus = new StringButton(8, height - 20, 70, 20, false, () -> {
            IrcState state = MineTogetherChat.CHAT_STATE.getIrcClient().getState();
            return new TextComponent(STATE_FORMAT_LOOKUP.get(state) + "\u2022" + " " + ChatFormatting.WHITE + STATE_DESC_LOOKUP.get(state));
        }, button -> { }));

        addRenderableWidget(friendsList = new Button(5, 5, 100, 20, new TranslatableComponent("minetogether:button.friends"), e -> minecraft.setScreen(new FriendsListScreen(this))));

        messageDropdownButton = addRenderableWidget(new DropdownButton<>(100, 20, clicked -> {
            assert clickedMessage != null;
            switch (clicked) {
                case MUTE -> clickedMessage.sender.mute();
                case ADD_FRIEND -> minecraft.setScreen(new FriendRequestScreen(this, clickedMessage.sender, FriendRequestScreen.Type.REQUEST));
                // TODO requires replacing known user names in to-be-sent messages.
//                case MENTION -> {
//                    String val = sendEditBox.getValue();
//                    if (!val.isEmpty() && val.charAt(val.length() - 1) != ' ') {
//                        val = val + " ";
//                    }
//                    sendEditBox.setValue(val + clickedMessage.sender.getDisplayName());
//                }
                default -> LOGGER.info("Dropdown action not currently implemented! {}", clicked);
            }
        }));
        messageDropdownButton.setEntries(MessageDropdownOption.VALUES);
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
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(1);
        super.render(poseStack, i, j, f);
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

        ChatScrollList.ChatLine line = chatList.getEntry(mouseX, mouseY);
        if (line == null) return false;
        if (line.message.sender == null) return false;
        if (line.message.sender == MineTogetherChat.getOurProfile()) return false;

        Style style = minecraft.font.getSplitter().componentStyleAtWidth(line.getComponent(), (int) mouseX);
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
