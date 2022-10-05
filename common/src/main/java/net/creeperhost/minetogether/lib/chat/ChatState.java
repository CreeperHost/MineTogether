package net.creeperhost.minetogether.lib.chat;

import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.web.ApiClient;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 14/9/22.
 */
public class ChatState {

    public final ApiClient api;
    public final ChatAuth auth;
    public final MutedUserList mutedUserList;
    public final ProfileManager profileManager;

    @Nullable
    private IrcClient ircClient;

    public ChatState(ApiClient api, ChatAuth auth, MutedUserList mutedUserList) {
        this.api = api;
        this.auth = auth;
        this.mutedUserList = mutedUserList;
        profileManager = new ProfileManager(this, auth.getHash());
    }

    public void setIrcClient(IrcClient ircClient) {
        assert this.ircClient == null : "IrcClient already set.";

        this.ircClient = ircClient;
    }

    public IrcClient getIrcClient() {
        return requireNonNull(ircClient, "IrcClient not set yet.");
    }
}
