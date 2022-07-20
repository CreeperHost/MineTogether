package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.message.MessageComponent;
import net.creeperhost.minetogether.lib.chat.message.MessageUtils;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import org.apache.commons.lang3.tuple.Pair;
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

    private final List<Message> messages = new ArrayList<>();
    private final List<Message> messagesView = Collections.unmodifiableList(messages);

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
    public List<Message> getMessages() {
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
        addMessage(new Message(
                timestamp,
                sender,
                MessageComponent.of(sender),
                MessageUtils.parseMessage(client.getProfileManager(), message)
        ));
    }

    public void addNoticeMessage(Instant timestamp, String message) {
        Pair<MessageComponent, MessageComponent> pair = MessageUtils.parseSystemMessage(client.getProfileManager(), message);
        addMessage(new Message(
                timestamp,
                null,
                pair.getLeft(),
                pair.getRight()
        ));
    }

    private void addMessage(Message message) {
        messages.add(message);
        for (ChatListener listener : listeners) {
            listener.newMessage(message);
        }
    }

}
