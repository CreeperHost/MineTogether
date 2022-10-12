package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.DisplayableMessage;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author covers1624
 */
public class ChatScrollList extends AbstractSelectionList<ChatScrollList.ChatLine> {

    @Nullable
    private IrcChannel channel;
    @Nullable
    private IrcChannel.ChatListener listener;

    private final List<Message> pendingMessages = new LinkedList<>();
    private final List<ScrollListDisplayableMessage> messages = new LinkedList<>();

    public ChatScrollList(Minecraft minecraft, int width, int height, int y0, int y1) {
        super(minecraft, width, height, y0, y1, 10);
    }

    public void attach(IrcChannel channel) {
        this.channel = channel;
        pendingMessages.addAll(channel.getMessages());
        listener = channel.addListener(e -> {
            synchronized (pendingMessages) {
                pendingMessages.add(e);
            }
        });
    }

    private void addMessage(Message message) {
        boolean maxScroll = getScrollAmount() == getMaxScroll();
        ScrollListDisplayableMessage newMessage = new ScrollListDisplayableMessage(message);
        messages.add(newMessage);
        newMessage.display();
        if (maxScroll) {
            setScrollAmount(getMaxScroll());
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (!pendingMessages.isEmpty()) {
            synchronized (pendingMessages) {
                for (Message pendingMessage : pendingMessages) {
                    addMessage(pendingMessage);
                }
                pendingMessages.clear();
            }
        }
        super.render(poseStack, i, j, f);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    protected int getScrollbarPosition() {
        return width + 2;
    }

    public void removed() {
        if (channel != null) {
            assert listener != null;
            channel.removeListener(listener);
        }
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    // TODO replace with AT.
    @Nullable
    public ChatLine getEntry(double mouseX, double mouseY) {
        return getEntryAtPosition(mouseX, mouseY);
    }

    private class ScrollListDisplayableMessage extends DisplayableMessage<ChatLine> {

        private ScrollListDisplayableMessage(Message message) {
            super(message);
        }

        @Override
        public void display() {
            format();
            children().addAll(getTrimmedLines());
        }

        @Override
        protected boolean isForward() {
            return true;
        }

        @Override
        protected ChatLine createMessage(int addTime, FormattedCharSequence message) {
            return new ChatLine(ChatScrollList.this, getMessage(), message);
        }

        @Override
        protected int getMessageIndex(ChatLine message) {
            return children().indexOf(message);
        }

        @Override
        protected void clearMessages() {
            getTrimmedLines().forEach(ChatScrollList.this::removeEntry);
        }

        @Override
        protected void addMessage(int index, ChatLine message) {
            children().add(index, message);
        }

        @Override
        protected double getChatWidth() {
            return getRowWidth();
        }
    }

    public static class ChatLine extends Entry<ChatLine> {

        private final ChatScrollList parent;
        public final FormattedCharSequence formattedMessage;
        public final Message message;

        public ChatLine(ChatScrollList parent, Message message, FormattedCharSequence formattedMessage) {
            this.parent = parent;
            this.message = message;
            this.formattedMessage = formattedMessage;
        }

        @Override
        public void render(PoseStack poseStack, int idx, int top, int left, int width, int height, int mx, int my, boolean hovered, float partialTicks) {
            drawString(poseStack, parent.minecraft.font, formattedMessage, left, top, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            return false; // Prevent dragging.
        }
    }
}
