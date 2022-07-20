package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;

/**
 * @author covers1624
 */
public class ChatScrollList extends AbstractSelectionList<ChatScrollList.ChatLine> {

    private final IrcChannel channel;
    private final IrcChannel.ChatListener listener;

    public ChatScrollList(Minecraft minecraft, int width, int height, int y0, int y1, IrcChannel channel) {
        super(minecraft, width, height, y0, y1, 10);
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
        channel.removeListener(listener);
    }

    public static class ChatLine extends Entry<ChatLine> {

        private final ChatScrollList parent;
        private final Message message;

        private String line;

        public ChatLine(ChatScrollList parent, Message message) {
            this.parent = parent;
            this.message = message;
            message.addListener(this, (i, e) -> i.line = updateLine());
            line = updateLine();
        }

        private String updateLine() {
            return message.senderName + ": " + message.getMessage();
        }

        @Override
        public void render(PoseStack poseStack, int idx, int top, int left, int width, int height, int mx, int my, boolean bl, float partialTicks) {
            drawString(poseStack, parent.minecraft.font, line, left, top, 0xFFFFFFFF);
        }
    }
}
