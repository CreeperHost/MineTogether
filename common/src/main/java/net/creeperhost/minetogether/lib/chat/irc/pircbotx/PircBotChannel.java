package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.irc.AbstractChannel;
import net.creeperhost.minetogether.lib.chat.message.MessageUtils;
import org.jetbrains.annotations.Nullable;
import org.pircbotx.Channel;

import java.time.Instant;

/**
 * Created by covers1624 on 29/6/22.
 */
public class PircBotChannel extends AbstractChannel {

    @Nullable
    private Channel channel;

    public PircBotChannel(ChatState chatState, String name) {
        super(chatState, name);
    }

    @Override
    public void sendMessage(String message) {
        if (channel != null) {
            channel.send().message(MessageUtils.processOutboundMessage(chatState.profileManager, message));
            addMessage(Instant.now(), chatState.profileManager.getOwnProfile(), message);
        }
    }

    public void bindChannel(@Nullable Channel channel) {
        this.channel = channel;
    }
}
