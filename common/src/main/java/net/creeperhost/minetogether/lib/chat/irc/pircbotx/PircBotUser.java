package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

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
    @Nullable
    private User ircUser;

    public PircBotUser(PircBotX client, Profile profile) {
        this.client = client;
        this.profile = profile;
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
    public void sendRawCTCP(String command) {
        if (ircUser == null) throw new IllegalStateException("User is not online");

        client.sendIRC().ctcpCommand(ircUser.getNick(), command);
    }
}
