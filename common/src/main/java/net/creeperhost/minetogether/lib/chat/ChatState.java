package net.creeperhost.minetogether.lib.chat;

import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.PircBotClient;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.web.ApiClient;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Created by covers1624 on 14/9/22.
 */
public class ChatState {

    public final ApiClient api;
    public final ChatAuth auth;
    public final MutedUserList mutedUserList;
    public final ProfileManager profileManager;
    public final IrcClient ircClient;
    public final boolean logChatToConsole;

    @VisibleForTesting
    protected ChatState(ApiClient api, ChatAuth auth, MutedUserList mutedUserList, ProfileManager profileManager, IrcClient ircClient, boolean logChatToConsole) {
        this.api = api;
        this.auth = auth;
        this.mutedUserList = mutedUserList;
        this.profileManager = profileManager;
        this.ircClient = ircClient;
        this.logChatToConsole = logChatToConsole;
    }

    public ChatState(ApiClient api, ChatAuth auth, MutedUserList mutedUserList, String realName, boolean logChatToConsole) {
        this.api = api;
        this.auth = auth;
        this.mutedUserList = mutedUserList;
        profileManager = new ProfileManager(auth.getHash()) {
            @Override
            public ChatState getChatState() {
                return ChatState.this;
            }
        };
        profileManager.getOwnProfile().setPack(realName);
        ircClient = new PircBotClient(this, realName);
        this.logChatToConsole = logChatToConsole;

        // Look these up ahead of time. May as well.
        for (String mutedUser : mutedUserList.getMutedUsers()) {
            profileManager.lookupProfileStale(mutedUser);
        }
    }
}
