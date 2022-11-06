package net.creeperhost.minetogether.lib.chat.irc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.message.MessageComponent;
import net.creeperhost.minetogether.lib.chat.message.MessageUtils;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by covers1624 on 17/10/22.
 */
public abstract class AbstractChannel implements IrcChannel {

    public static final ExecutorService SEND_EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("irc-send-queue")
                    .setDaemon(true)
                    .build()
    );

    protected final ChatState chatState;
    protected final String name;

    private final List<ChatListener> listeners = new LinkedList<>();

    private final List<Message> messages = new ArrayList<>();
    private final List<Message> messagesView = Collections.unmodifiableList(messages);

    protected AbstractChannel(ChatState chatState, String name) {
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
    public ChatListener addListener(ChatListener listener) {
        listeners.add(listener);
        return listener;
    }

    @Override
    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
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
