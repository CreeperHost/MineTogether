package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 12/10/22.
 */
public abstract class DisplayableMessage<M> {

    private final Minecraft mc = Minecraft.getInstance();
    private final Message message;
    private final Object listener;
    private final List<M> trimmedLines = new LinkedList<>();
    @Nullable
    private GuiMessage builtMessage;

    protected DisplayableMessage(Message message) {
        this.message = message;
        listener = message.addListener(this, (i, e) -> i.onChange());
    }

    public void onDead() {
        message.removeListener(listener);
    }

    public void display() {
        format();
        insertAt(0);
    }

    public void format() {
        int addTime = builtMessage != null ? builtMessage.addedTime() : mc.gui.getGuiTicks();
        builtMessage = new GuiMessage(addTime, MessageFormatter.formatMessage(message), null, null);

        int maxLen = Mth.floor(getChatWidth());
        List<FormattedCharSequence> lines = ComponentRenderUtils.wrapComponents(builtMessage.content(), maxLen, mc.font);
        for (FormattedCharSequence line : lines) {
            trimmedLines.add(createMessage(addTime, line));
        }

        // Why would we ever build no lines?
        assert !trimmedLines.isEmpty();
    }

    public List<M> getTrimmedLines() {
        return trimmedLines;
    }

    @Nullable
    public Component getBuiltMessage() {
        return builtMessage.content();
    }

    public Message getMessage() {
        return message;
    }

    private void onChange() {
        // Message has not been displayed yet.
        if (builtMessage == null) return;

        // If we are running in forward mode (oldest at index 0), we want message in index 0.
        // If we are running in backwards mode (oldest at index size - 1), we want message in index size - 1.
        int trimmedIdx = getMessageIndex(isForward() ? trimmedLines.get(0) : trimmedLines.get(trimmedLines.size() - 1));
        clearMessages();

        format();
        insertAt(trimmedIdx);
    }

    private void insertAt(int trimmedIdx) {
        for (M trimmedLine : trimmedLines) {
            addMessage(trimmedIdx, trimmedLine);
        }
    }

    protected abstract boolean isForward();

    protected abstract M createMessage(int addTime, FormattedCharSequence message);

    protected abstract int getMessageIndex(M message);

    protected abstract void clearMessages();

    protected abstract void addMessage(int index, M message);

    protected abstract double getChatWidth();
}
