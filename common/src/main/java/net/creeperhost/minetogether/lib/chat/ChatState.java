package net.creeperhost.minetogether.lib.chat;

import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.PircBotClient;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.web.ApiClient;

/**
 * Created by covers1624 on 14/9/22.
 */
public class ChatState {

    public final ApiClient api;
    public final ChatAuth auth;
    public final MutedUserList mutedUserList;
    public final ProfileManager profileManager;
    public final IrcClient ircClient;

    public ChatState(ApiClient api, ChatAuth auth, MutedUserList mutedUserList, String realName) {
        this.api = api;
        this.auth = auth;
        this.mutedUserList = mutedUserList;
        profileManager = new ProfileManager(this, auth.getHash());
        ircClient = new PircBotClient(this, realName);
    }
}