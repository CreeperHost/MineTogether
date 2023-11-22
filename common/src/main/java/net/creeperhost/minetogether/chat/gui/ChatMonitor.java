package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 23/09/2023
 */
public class ChatMonitor {
    @Nullable
    private IrcChannel channel;
    @Nullable
    private IrcChannel.ChatListener listener;

    private final List<Message> pendingMessages = new LinkedList<>();
    private final List<Message> messages = new LinkedList<>();
    @Nullable
    private Consumer<List<Message>> onMessagesUpdated;

    public void onMessagesUpdated(Consumer<List<Message>> onMessagesUpdated) {
        this.onMessagesUpdated = onMessagesUpdated;
    }

    public void attach(@Nullable IrcChannel channel) {
        if (this.channel == channel) return;

        if (this.channel != null) {
            assert listener != null;
            this.channel.removeListener(listener);
            listener = null;
        }
        pendingMessages.clear();
        messages.clear();
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

    @Nullable
    public IrcChannel getChannel() {
        return channel;
    }

    public void tick() {
        if (!pendingMessages.isEmpty()) {
            synchronized (pendingMessages) {
                messages.addAll(pendingMessages);
                pendingMessages.clear();
            }
            if (onMessagesUpdated != null) {
                onMessagesUpdated.accept(messages);
            }
        }
    }

    public void onGuiClose() {
        if (channel != null) {
            assert listener != null;
            channel.removeListener(listener);
        }
    }
}
