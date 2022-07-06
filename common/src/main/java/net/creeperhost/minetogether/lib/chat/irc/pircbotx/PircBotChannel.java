package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import org.jetbrains.annotations.Nullable;
import org.pircbotx.Channel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 29/6/22.
 */
public class PircBotChannel implements IrcChannel {

    private final PircBotClient client;
    private final String name;

    private final List<ChatListener> listeners = new LinkedList<>();

    private final List<String> messages = new ArrayList<>();
    private final List<String> messagesView = Collections.unmodifiableList(messages);

    @Nullable
    private Channel channel;

    public PircBotChannel(PircBotClient client, String name) {
        this.client = client;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getMessages() {
        return messagesView;
    }

    @Override
    public void sendMessage(String message) {
        if (channel != null) {
            channel.send().message(message);
            addMessage(Instant.now(), client.getUserProfile(), message);
        }
    }

    @Override
    public ChatListener addListener(ChatListener listener) {
        listeners.add(listener);
        return listener;
    }

    @Override
    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
    }

    public void bindChannel(@Nullable Channel channel) {
        this.channel = channel;
    }

    public void addMessage(Instant timestamp, Profile sender, String message) {
        message = sender.getDisplayName() + ": " + message;
        int index = messages.size();
        messages.add(message);
        onNewMessage(message, index);
    }

    public void addNoticeMessage(Instant timestamp, String message) {
        int index = messages.size();
        messages.add(message);
        onNewMessage(message, index);
    }

    private void onNewMessage(String message, int index) {
        for (ChatListener listener : listeners) {
            listener.newMessage(message, index);
        }
    }

}
