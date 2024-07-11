package net.creeperhost.minetogether.chat.ingame;

import net.covers1624.quack.collection.FastStream;
import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.DisplayableMessage;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.message.MessageComponent;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by covers1624 on 20/7/22.
 */
public class MTChatComponent extends ChatComponent {

    private static final Logger LOGGER = LogManager.getLogger();

    // TODO tweak this.
    private static final int MAX_MESSAGE_HISTORY = 150;

    private final ChatTarget target;
    private final Minecraft minecraft;

    // Flag to override explicitly delegating to vanilla, set in specific cases.
    private boolean internalUpdate = false;

    private final List<InGameDisplayableMessage> changedMessages = new LinkedList<>();
    private final LinkedList<Message> pendingMessages = new LinkedList<>();
    private final List<InGameDisplayableMessage> processedMessages = new ArrayList<>();
    @Nullable
    private IrcChannel channel;

    @Nullable
    private Message clickedMessage;

    public MTChatComponent(ChatTarget target, Minecraft minecraft) {
        super(minecraft);
        this.target = target;
        this.minecraft = minecraft;
        assert target != ChatTarget.VANILLA : "MTChatComponent doesn't work this way";
    }

    public void attach(IrcChannel channel) {
        // Bail if we are already bound. IrcChannel's are
        if (this.channel != null) return;

        this.channel = channel;
        channel.addListener(message -> {
            synchronized (pendingMessages) {
                pendingMessages.add(message);
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        // Fallback to force bind the channel if it is null.
        if (channel == null && target == ChatTarget.PUBLIC) {
            IrcChannel channel = MineTogetherChat.CHAT_STATE.ircClient.getPrimaryChannel();
            if (channel != null) {
                attach(channel);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, int k, boolean bl) {
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
        if (!changedMessages.isEmpty()) {
            synchronized (changedMessages) {
                for (InGameDisplayableMessage changedMessage : changedMessages) {
                    changedMessage.reformat();
                }
                changedMessages.clear();
            }
        }
        super.render(graphics, i, j, k, bl);
    }

    @Override
    public void rescaleChat() {
        // If we are the target, propagate to the others.
        if (MineTogetherChat.getTarget() == target) {
            MineTogetherChat.vanillaChat.rescaleChat();
        }

        trimmedMessages.clear();
        resetChatScroll();

        for (InGameDisplayableMessage message : processedMessages) {
            message.format();
            message.display();
        }
    }

    @Override
    public void clearMessages(boolean bl) {
        // If we are the target, propagate to the others.
        if (MineTogetherChat.getTarget() == target) {
            MineTogetherChat.vanillaChat.clearMessages(bl);
        }

        // Clear all messages. Do this manually as we don't use all the fields from ChatComponent.
        // This is more explicit on how we operate.
        synchronized (pendingMessages) {
            pendingMessages.clear();
        }
        for (InGameDisplayableMessage message : processedMessages) {
            message.onDead();
        }
        processedMessages.clear();
        trimmedMessages.clear();
    }

    @Override
    public void addRecentChat(String string) {
        if (channel == null) {
            LOGGER.error("Can't send message, chat is not bound!");
            return;
        }
        channel.sendMessage(string);
    }

    private void addMessage(Message message) {
        InGameDisplayableMessage newMessage = new InGameDisplayableMessage(message);
        processedMessages.add(newMessage);
        newMessage.display();

        if (isChatFocused() && chatScrollbarPos > 0) {
            newMessageSinceScroll = true;
            scrollChat(1);
        }

        while (processedMessages.size() > MAX_MESSAGE_HISTORY) {
            InGameDisplayableMessage toRemove = processedMessages.remove(0);
            trimmedMessages.removeAll(toRemove.getTrimmedLines());
            toRemove.onDead();
        }
    }

    /**
     * Adds a message directly to the MT chat window bypassing IRC.
     * This is a client side only message.
     * <p>
     * Warning, This implementation is kinda hacky and may not be entirely reliable.
     *
     * @param message   The message. or null to remove a previous message.
     * @param signature The message signature, If a previous message exists with this signature it will be removed before the new message is added.
     */
    public void localMessage(@Nullable Component message, MessageSignature signature) {
        LocalMessage oldMessage = FastStream.of(processedMessages)
                .filter(e -> e instanceof LocalMessage msg && msg.signature.equals(signature))
                .map(e -> (LocalMessage) e)
                .findFirst()
                .orElse(null);

        if (oldMessage != null) {
            processedMessages.remove(oldMessage);
            if (oldMessage.line != null) {
                trimmedMessages.remove(oldMessage.line);
            }
        }

        if (message == null) return;

        InGameDisplayableMessage newMessage = new LocalMessage(message, signature);
        processedMessages.add(newMessage);
        newMessage.display();

        if (isChatFocused() && chatScrollbarPos > 0) {
            newMessageSinceScroll = true;
            scrollChat(1);
        }

        while (processedMessages.size() > MAX_MESSAGE_HISTORY) {
            InGameDisplayableMessage toRemove = processedMessages.remove(0);
            trimmedMessages.removeAll(toRemove.getTrimmedLines());
            toRemove.onDead();
        }
    }

    @Override
    public void addMessage(Component component) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component);
    }

    @Override
    public void addMessage(Component component, @Nullable MessageSignature messageSignature, @Nullable GuiMessageTag guiMessageTag) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component, messageSignature, guiMessageTag);
    }

    @Override
    public void deleteMessage(MessageSignature messageSignature) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.deleteMessage(messageSignature);
    }

    public boolean handleClick(double mouseX, double mouseY) {
        if (!isChatFocused()) return false;

        double x = mouseX - 2.0;
        double y = (double) minecraft.getWindow().getGuiScaledHeight() - mouseY - 40.0;
        x = Mth.floor(x / getScale());
        y = Mth.floor(y / (getScale() * (minecraft.options.chatLineSpacing().get() + 1.0)));
        if (x < 0.0 || y < 0.0) return false;

        int i = Math.min(getLinesPerPage(), trimmedMessages.size());
        if (x <= (double) Mth.floor((double) getWidth() / getScale())) {
            Objects.requireNonNull(minecraft.font);
            if (y < (double) (9 * i + i)) {
                Objects.requireNonNull(minecraft.font);
                int j = (int) (y / 9.0 + (double) chatScrollbarPos);
                if (j >= 0 && j < trimmedMessages.size()) {
                    return handleClickedMessage(findMessageForTrimmedMessage(trimmedMessages.get(j)), x);
                }
            }
        }

        return false;
    }

    // TOOD unify with above and bellow.
    @Nullable
    public Style getStyleUnderMouse(double mouseX, double mouseY) {
        if (!isChatFocused()) return null;

        double x = mouseX - 2.0;
        double y = (double) minecraft.getWindow().getGuiScaledHeight() - mouseY - 40.0;
        x = Mth.floor(x / getScale());
        y = Mth.floor(y / (getScale() * (minecraft.options.chatLineSpacing().get() + 1.0)));
        if (x < 0.0 || y < 0.0) return null;

        int i = Math.min(getLinesPerPage(), trimmedMessages.size());
        if (x <= (double) Mth.floor((double) getWidth() / getScale())) {
            Objects.requireNonNull(minecraft.font);
            if (y < (double) (9 * i + i)) {
                Objects.requireNonNull(minecraft.font);
                int j = (int) (y / 9.0 + (double) chatScrollbarPos);
                if (j >= 0 && j < trimmedMessages.size()) {
                    InGameDisplayableMessage message = findMessageForTrimmedMessage(trimmedMessages.get(j));
                    if (message == null) return null;
                    return minecraft.font.getSplitter().componentStyleAtWidth(message.getBuiltMessage(), (int) x);
                }
            }
        }
        return null;
    }

    private boolean handleClickedMessage(@Nullable InGameDisplayableMessage clickedMessage, double x) {
        if (clickedMessage == null) return false;

        Message message = clickedMessage.getMessage();
        if (message.sender == null) return false;
        if (message.sender == MineTogetherChat.getOurProfile()) return false;

        Style style = minecraft.font.getSplitter().componentStyleAtWidth(clickedMessage.getBuiltMessage(), (int) x);
        if (style == null) return false;
        ClickEvent event = style.getClickEvent();
        if (event == null) return false;
        if (!event.getValue().equals(MessageFormatter.CLICK_NAME)) return false;

        this.clickedMessage = message;
        return true;
    }

    @Nullable
    private InGameDisplayableMessage findMessageForTrimmedMessage(GuiMessage.Line trimmedMessage) {
        // Little slow, realistically we should have a lookup map, but would be a pain to maintain.
        // This searches from the most recent chat message to the oldest.
        for (InGameDisplayableMessage processedMessage : processedMessages) {
            if (processedMessage.getTrimmedLines().contains(trimmedMessage)) {
                return processedMessage;
            }
        }
        return null;
    }

    @Nullable
    public Message getClickedMessage() {
        return clickedMessage;
    }

    public void clearClickedMessage() {
        clickedMessage = null;
    }

    private class InGameDisplayableMessage extends DisplayableMessage<GuiMessage.Line> {

        private InGameDisplayableMessage(Message message) {
            super(message);
        }

        @Override
        protected void onModified() {
            synchronized (changedMessages) {
                changedMessages.add(this);
            }
        }

        @Override
        protected boolean isForward() {
            return false;
        }

        @Override
        protected GuiMessage.Line createMessage(int addTime, FormattedCharSequence message) {
            return new GuiMessage.Line(addTime, message, null, true);// TODO true is not correct here, we need to pass it down.
        }

        @Override
        protected int getMessageIndex(GuiMessage.Line message) {
            return trimmedMessages.indexOf(message);
        }

        @Override
        protected void clearMessages() {
            trimmedMessages.removeAll(getTrimmedLines());
        }

        @Override
        protected void addMessage(int index, GuiMessage.Line message) {
            trimmedMessages.add(index, message);
        }

        @Override
        protected double getChatWidth() {
            return (double) getWidth() / getScale();
        }
    }

    private class LocalMessage extends InGameDisplayableMessage {
        private final Component message;
        private final MessageSignature signature;
        @Nullable
        private GuiMessage.Line line;

        private LocalMessage(Component message, MessageSignature signature) {
            super(new Message(Instant.now(), MineTogetherChat.getOurProfile(), MessageComponent.of(), MessageComponent.of()));
            this.message = message;
            this.signature = signature;
        }

        @Override
        protected GuiMessage.Line createMessage(int addTime, FormattedCharSequence formattedCharSequence) {
            return line = new GuiMessage.Line(addTime, message.getVisualOrderText(), null, true);
        }
    }
}
