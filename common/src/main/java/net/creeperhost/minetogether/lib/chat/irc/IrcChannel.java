package net.creeperhost.minetogether.lib.chat.irc;

import net.creeperhost.minetogether.lib.chat.message.Message;

import java.util.List;

/**
 * Created by covers1624 on 29/6/22.
 */
public interface IrcChannel {

    /**
     * Gets the name for this channel as advertised by
     * the IRC server.
     * <p>
     * If this channel is a direct message, it will be
     * the nickname of the recipient.
     *
     * @return The name.
     */
    String getName();

    /**
     * Gets the messages this channel contains.
     *
     * @return The messages.
     */
    List<Message> getMessages();

    /**
     * Send a message to this channel.
     *
     * @param message The message to send.
     */
    void sendMessage(String message);

    /**
     * Add a listener to this channel.
     *
     * @param listener The listener.
     */
    // TODO, i hate add/remove here, needs 'instance' object to watch, so we dont have hundreds of
    //       listeners being fired holding objects in memory.
    ChatListener addListener(ChatListener listener);

    /**
     * Remove a listener from this channel.
     *
     * @param listener The listener.
     */
    void removeListener(ChatListener listener);

    /**
     * Represents an interface capable of listening to chat messages.
     */
    interface ChatListener {

        /**
         * Called when a new message appears.
         *
         * @param message The message.
         */
        void newMessage(Message message);
    }
}
