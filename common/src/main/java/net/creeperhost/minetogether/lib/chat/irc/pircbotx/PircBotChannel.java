package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.ChatState;
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

    private final ChatState chatState;
    private final String name;

    private final List<ChatListener> listeners = new LinkedList<>();

    private final List<Message> messages = new ArrayList<>();
    private final List<Message> messagesView = Collections.unmodifiableList(messages);

    @Nullable
    private Channel channel;

    public PircBotChannel(ChatState chatState, String name) {
        this.chatState = chatState;
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
            addMessage(Instant.now(), chatState.profileManager.getOwnProfile(), message);
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
                MessageUtils.parseMessage(chatState.profileManager, message)
        ));
    }

    public void addNoticeMessage(Instant timestamp, String message) {
        Pair<MessageComponent, MessageComponent> pair = MessageUtils.parseSystemMessage(chatState.profileManager, message);
        addMessage(new Message(
                timestamp,
                null,
                pair.getLeft(),
                pair.getRight()
        ));
    }

    private void addMessage(Message message) {
        // Yeet messages from muted users.
        if (message.sender != null && message.sender.isMuted()) return;

        if (message.sender != null) {
            message.sender.addSentMessage(message);
        }
        messages.add(message);
        for (ChatListener listener : listeners) {
            listener.newMessage(message);
        }
    }

}
