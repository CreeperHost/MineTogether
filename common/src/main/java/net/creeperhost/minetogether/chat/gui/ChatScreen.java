package net.creeperhost.minetogether.chat.gui;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.polylib.gui.StringButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * @author covers1624
 */
public class ChatScreen extends Screen {

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
    private final IrcChannel channel;

    @Nullable
    private ChatScrollList chatList;
    @Nullable
    private EditBox sendEditBox;

    @Nullable
    private StringButton connectionStatus;

    public ChatScreen(Screen parent) {
        super(new TextComponent("MineTogether Chat"));
        this.parent = parent;
        channel = MineTogetherChat.getIrcClient().getPrimaryChannel();
    }

    @Override
    protected void init() {
        assert minecraft != null;

        chatList = new ChatScrollList(minecraft, width - 20, height - 50, 30, height - 50, channel);
        chatList.setLeftPos(10);
        chatList.setScrollAmount(chatList.getMaxScroll());
        boolean shouldFocusEditBox = sendEditBox == null || sendEditBox.isFocused();
        sendEditBox = new EditBox(minecraft.font, 11, height - 48, width - 22, 20, sendEditBox, new TextComponent(""));
        sendEditBox.setFocus(shouldFocusEditBox);
        sendEditBox.setMaxLength(256);

        addRenderableWidget(chatList);
        addRenderableWidget(sendEditBox);

        addRenderableWidget(new Button(width - 100 - 5, height - 5 - 20, 100, 20, new TextComponent("Cancel"), button -> {
            minecraft.setScreen(parent);
        }));

        addRenderableWidget(connectionStatus = new StringButton(8, height - 20, 70, 20, false, () -> {
            IrcState state = MineTogetherChat.getIrcClient().getState();
            return new TextComponent(STATE_FORMAT_LOOKUP.get(state) + "\u2022" + " " + ChatFormatting.WHITE + STATE_DESC_LOOKUP.get(state));
        }, button -> { }));
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
                channel.sendMessage(trimmedMessage);
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}