package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.irc.AbstractChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcUser;
import net.creeperhost.minetogether.lib.chat.message.MessageUtils;

import java.time.Instant;

/**
 * Created by covers1624 on 17/10/22.
 */
public class PMChannel extends AbstractChannel {

    private final IrcUser user;

    public PMChannel(ChatState chatState, IrcUser user) {
        super(chatState, "#MT" + user.getProfile().getIrcName());
        this.user = user;
    }

    @Override
    public void sendMessage(String message) {
        if (user.isOnline()) {
            user.sendMessage(MessageUtils.processOutboundMessage(chatState.profileManager, message));
            addMessage(Instant.now(), chatState.profileManager.getOwnProfile(), message);
        }
    }
}
