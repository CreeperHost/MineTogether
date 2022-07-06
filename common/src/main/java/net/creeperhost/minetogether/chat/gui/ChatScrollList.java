package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
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
        for (String message : channel.getMessages()) {
            addEntry(new ChatLine(this, message));
        }
        listener = channel.addListener((message, index) -> {
            boolean maxScroll = getScrollAmount() == getMaxScroll();
            addEntry(new ChatLine(ChatScrollList.this, message));
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
        private final String chatMessage;

        public ChatLine(ChatScrollList parent, String chatMessage) {
            this.parent = parent;
            this.chatMessage = chatMessage;
        }

        @Override
        public void render(PoseStack poseStack, int idx, int top, int left, int width, int height, int mx, int my, boolean bl, float partialTicks) {
            drawString(poseStack, parent.minecraft.font, chatMessage, left, top, 0xFFFFFFFF);
        }
    }
}
