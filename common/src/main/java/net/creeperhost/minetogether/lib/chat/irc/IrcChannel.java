package net.creeperhost.minetogether.lib.chat.irc;

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
    // TODO this should be swapped out for some parsed linked list which
    //      properly represents user mentions, etc.
    // MessageHead -> [message, ...]
    // message.append
    // message.replace(a) MessageHead.clear, MessageHead.addChild(a)
    List<String> getMessages();

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
         * @param index   The index of the message.
         *                // TODO, Index may be redundant, as new messages are always at the end.
         */
        void newMessage(String message, int index);
    }
}