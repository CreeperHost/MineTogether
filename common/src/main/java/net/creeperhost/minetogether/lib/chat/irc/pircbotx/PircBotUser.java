package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcUser;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import org.jetbrains.annotations.Nullable;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

/**
 * Created by covers1624 on 14/9/22.
 */
public class PircBotUser implements IrcUser {

    private final PircBotX client;
    private final Profile profile;
    private final PMChannel channel;
    @Nullable
    private User ircUser;

    public PircBotUser(PircBotX client, ChatState state, Profile profile) {
        this.client = client;
        this.profile = profile;
        this.channel = new PMChannel(state, this);
    }

    void bindIrcUser(@Nullable User ircUser) {
        this.ircUser = ircUser;
    }

    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public boolean isOnline() {
        return ircUser != null;
    }

    @Override
    public IrcChannel getChannel() {
        return channel;
    }

    @Override
    public void sendMessage(String message) {
        if (ircUser != null) {
            ircUser.send().message(message);
        }
    }

    @Override
    public void sendRawCTCP(String command) {
        if (ircUser == null) throw new IllegalStateException("User is not online");

        client.sendIRC().ctcpCommand(ircUser.getNick(), command);
    }
}
