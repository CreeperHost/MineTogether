package net.creeperhost.minetogether.chat.ingame;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 20/7/22.
 */
public class MTChatComponent extends ChatComponent {

    // TODO tweak this.
    private static final int MAX_MESSAGE_HISTORY = 150;

    private final ChatTarget target;
    private final Minecraft minecraft;

    // Flag to override explicitly delegating to vanilla, set in specific cases.
    private boolean internalUpdate = false;

    private final LinkedList<Message> pendingMessages = new LinkedList<>();
    private final List<DisplayableMessage> processedMessages = new ArrayList<>();
    private IrcChannel channel;

    public MTChatComponent(ChatTarget target, Minecraft minecraft) {
        super(minecraft);
        this.target = target;
        this.minecraft = minecraft;
        assert target != ChatTarget.VANILLA : "MTChatComponent doesn't work this way";
    }

    public void attach(IrcChannel channel) {
        this.channel = channel;
        channel.addListener(message -> {
            synchronized (pendingMessages) {
                pendingMessages.add(message);
            }
        });
    }

    @Override
    public void render(PoseStack poseStack, int i) {
        if (!pendingMessages.isEmpty()) {
            internalUpdate = true;
            synchronized (pendingMessages) {
                for (Message pendingMessage : pendingMessages) {
                    addMessage(pendingMessage);
                }
                pendingMessages.clear();
            }
            internalUpdate = false;
        }
        super.render(poseStack, i);
    }

    @Override
    public void rescaleChat() {
        // Only delegate if we are the target to prevent loops.
        if (MineTogetherChat.target != target) {
            switch (MineTogetherChat.target) {
                case VANILLA -> MineTogetherChat.vanillaChat.rescaleChat();
                case PUBLIC -> MineTogetherChat.publicChat.rescaleChat();
            }
        }

        trimmedMessages.clear();
        resetChatScroll();

        for (DisplayableMessage message : processedMessages) {
            message.format();
            message.display();
        }
    }

    @Override
    public void clearMessages(boolean bl) {
        // Only delegate if we are the target to prevent loops.
        if (MineTogetherChat.target != target) {
            switch (MineTogetherChat.target) {
                case VANILLA -> MineTogetherChat.vanillaChat.clearMessages(bl);
                case PUBLIC -> MineTogetherChat.publicChat.clearMessages(bl);
            }
        }

        // Clear all messages. Do this manually as we don't use all the fields from ChatComponent.
        // This is more explicit on how we operate.
        synchronized (pendingMessages) {
            pendingMessages.clear();
        }
        for (DisplayableMessage message : processedMessages) {
            message.onDead();
        }
        processedMessages.clear();
        trimmedMessages.clear();
    }

    @Override
    public void addRecentChat(String string) {
        channel.sendMessage(string);
    }

    private void addMessage(Message message) {
        DisplayableMessage newMessage = new DisplayableMessage(message);
        processedMessages.add(newMessage);
        newMessage.display();

        if (isChatFocused() && chatScrollbarPos > 0) {
            newMessageSinceScroll = true;
            scrollChat(1);
        }

        while (processedMessages.size() > MAX_MESSAGE_HISTORY) {
            DisplayableMessage toRemove = processedMessages.remove(0);
            trimmedMessages.removeAll(toRemove.trimmedLines);
            toRemove.onDead();
        }
    }

    @Override
    public void removeById(int i) {
        assert !internalUpdate; // We dont use ID's

        MineTogetherChat.vanillaChat.removeById(i);
    }

    @Override
    public void addMessage(Component component, int i, int j, boolean bl) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component, i, j, bl);
    }

    @Override
    public void addMessage(Component component) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component);
    }

    @Override
    public void addMessage(Component component, int i) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component, i);
    }

    private class DisplayableMessage {

        private final Message message;
        private final Object listener;
        private final List<GuiMessage<FormattedCharSequence>> trimmedLines = new LinkedList<>();
        @Nullable
        private GuiMessage<Component> builtMessage;

        private DisplayableMessage(Message message) {
            this.message = message;
            listener = message.addListener(this, (i, e) -> i.onChange());
        }

        private void onDead() {
            message.removeListener(listener);
        }

        private void display() {
            format();
            insertAt(0);
        }

        private void onChange() {
            // Message has not been displayed yet.
            if (builtMessage == null) return;

            // Index of last trimmed line (closest to head of trimmedMessages list)
            int trimmedIdx = trimmedMessages.indexOf(trimmedLines.get(trimmedLines.size() - 1));
            format();

            trimmedMessages.removeAll(trimmedLines);
            insertAt(trimmedIdx);
        }

        private void insertAt(int trimmedIdx) {
            for (GuiMessage<FormattedCharSequence> trimmedLine : trimmedLines) {
                trimmedMessages.add(trimmedIdx, trimmedLine);
            }
        }

        private void format() {
            int addTime = builtMessage != null ? builtMessage.getAddedTime() : minecraft.gui.getGuiTicks();
            builtMessage = new GuiMessage<>(addTime, MessageFormatter.formatMessage(message), 0);

            int maxLen = Mth.floor((double) getWidth() / getScale());
            List<FormattedCharSequence> lines = ComponentRenderUtils.wrapComponents(builtMessage.getMessage(), maxLen, minecraft.font);
            for (FormattedCharSequence line : lines) {
                trimmedLines.add(new GuiMessage<>(addTime, line, 0));
            }

            // Why would we ever build no lines?
            assert !trimmedLines.isEmpty();
        }
    }
}
