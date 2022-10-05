package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * @author covers1624
 */
public class ChatScrollList extends AbstractSelectionList<ChatScrollList.ChatLine> {

    @Nullable
    private IrcChannel channel;
    @Nullable
    private IrcChannel.ChatListener listener;

    public ChatScrollList(Minecraft minecraft, int width, int height, int y0, int y1) {
        super(minecraft, width, height, y0, y1, 10);
    }

    public void attach(IrcChannel channel) {
        this.channel = channel;
        for (Message message : channel.getMessages()) {
            addEntry(new ChatLine(this, message));
        }
        listener = channel.addListener(m -> {
            boolean maxScroll = getScrollAmount() == getMaxScroll();
            addEntry(new ChatLine(ChatScrollList.this, m));
            if (maxScroll) {
                setScrollAmount(getMaxScroll());
            }
        });
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
        if (!this.isMouseOver(d, e)) {
            return false;
        }
        ChatLine entry = this.getEntryAtPosition(d, e);
        setSelected(entry);
        return true;
    }

    public static class ChatLine extends Entry<ChatLine> {

        private final ChatScrollList parent;
        public final Message message;

        private Component line;

        public ChatLine(ChatScrollList parent, Message message) {
            this.parent = parent;
            this.message = message;
            message.addListener(this, (i, e) -> i.line = updateLine());
            line = updateLine();
        }

        private Component updateLine() {
            return MessageFormatter.formatMessage(message);
        }

        @Override
        public void render(PoseStack poseStack, int idx, int top, int left, int width, int height, int mx, int my, boolean hovered, float partialTicks) {
            drawString(poseStack, parent.minecraft.font, line, left, top, 0xFFFFFFFF);
        }
    }
}
