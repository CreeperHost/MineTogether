package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.DisplayableMessage;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.polylib.gui.PreviewRenderer;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * @author covers1624
 */
public class ChatScrollList extends AbstractSelectionList<ChatScrollList.ChatLine> {

    public final int width;
    @Nullable
    private IrcChannel channel;
    @Nullable
    private IrcChannel.ChatListener listener;

    private final List<ScrollListDisplayableMessage> changedMessages = new LinkedList<>();
    private final List<Message> pendingMessages = new LinkedList<>();
    private final List<ScrollListDisplayableMessage> messages = new LinkedList<>();
    private final PreviewRenderer previewRenderer = new PreviewRenderer(5, 5, 80, 60) {
        @Override
        protected URL getUrlUnderMouse(int mouseX, int mouseY) {
            ChatLine line = getEntry(mouseX, mouseY);
            if (line == null) return null;
            Style style = minecraft.font.getSplitter().componentStyleAtWidth(line.formattedMessage, mouseX - getRowLeft());
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

    public ChatScrollList(Minecraft minecraft, int width, int height, int y0, int y1) {
        super(minecraft, width, height, y0, y1, 10);
        this.width = width;
    }

    public void attach(@Nullable IrcChannel channel) {
        if (this.channel != null) {
            assert listener != null;
            this.channel.removeListener(listener);
            listener = null;
        }
        pendingMessages.clear();
        for (ScrollListDisplayableMessage message : messages) {
            message.onDead();
        }
        messages.clear();
        clearEntries();

        this.channel = channel;
        if (channel != null) {
            pendingMessages.addAll(channel.getMessages());
            listener = channel.addListener(e -> {
                synchronized (pendingMessages) {
                    pendingMessages.add(e);
                }
            });
        }
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
    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        if (!pendingMessages.isEmpty()) {
            synchronized (pendingMessages) {
                for (Message pendingMessage : pendingMessages) {
                    addMessage(pendingMessage);
                }
                pendingMessages.clear();
            }
        }
        if (!changedMessages.isEmpty()) {
            synchronized (changedMessages) {
                for (ScrollListDisplayableMessage changedMessage : changedMessages) {
                    changedMessage.reformat();
                }
                changedMessages.clear();
            }
        }
        super.render(pStack, mouseX, mouseY, partialTicks);
        previewRenderer.render(pStack, mouseX, mouseY, partialTicks);
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
        for (ScrollListDisplayableMessage message : messages) {
            message.onDead();
        }
        messages.clear();
        clearEntries();
    }

    @Nullable
    public IrcChannel getChannel() {
        return channel;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    // TODO replace with AT's
    @Nullable
    public ChatLine getEntry(double mouseX, double mouseY) {
        return getEntryAtPosition(mouseX, mouseY);
    }

    public int getTop() {
        return y0;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
        protected void onModified() {
            synchronized (changedMessages) {
                changedMessages.add(this);
            }
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
