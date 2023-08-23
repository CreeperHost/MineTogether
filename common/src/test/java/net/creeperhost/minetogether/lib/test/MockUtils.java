package net.creeperhost.minetogether.lib.test;

import com.google.common.hash.Hashing;
import net.creeperhost.minetogether.lib.chat.ChatAuth;
import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.MutedUserList;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.irc.IrcUser;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.chat.util.HashLength;
import net.creeperhost.minetogether.lib.web.*;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.util.SignatureVerifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by covers1624 on 4/11/22.
 */
public class MockUtils {

    // TODO ChatState makes things WAAAY to interleaved. We need to replace all its methods with getters so mock can opt out of things
    //      and construct its components after ChatState.
    public static final UUID TEST_UUID = UUID.fromString("93664291-ecf3-48f5-813a-8065a4777a74");
    public static final String TEST_HASH = Hashing.sha256().hashString(TEST_UUID.toString(), UTF_8).toString().toUpperCase(Locale.ROOT);
    public static final String TEST_USER_HASH = "MT" + HashLength.MEDIUM.format(TEST_HASH);
    public static final ChatAuth MOCK_CHAT_AUTH = new MockChatAuth();
    public static final WebEngine MOCK_WEB_ENGINE = new MockWebEngine();
    public static final WebAuth MOCK_WEB_AUTH = new DynamicWebAuth();
    public static final ApiClient MOCK_API_CLIENT = ApiClient.builder().webAuth(MOCK_WEB_AUTH).webEngine(MOCK_WEB_ENGINE).build();
    public static final MutedUserList MOCK_MUTED_USERS = new MockMutedUserList(new HashSet<>());
    public static final ProfileManager MOCK_PROFILE_MANAGER = new MockProfileManager();
    public static final IrcClient MOCK_IRC_CLIENT = new MockIrcClient();
    public static final ChatState MOCK_CHAT_STATE = new MockChatState(
            MOCK_API_CLIENT,
            MOCK_CHAT_AUTH,
            MOCK_MUTED_USERS,
            MOCK_PROFILE_MANAGER,
            MOCK_IRC_CLIENT,
            false
    );

    // @formatter:off
    private static class MockChatState extends ChatState {
        public MockChatState(ApiClient api, ChatAuth auth, MutedUserList mutedUserList, ProfileManager profileManager, IrcClient ircClient, boolean logChatToConsole) {
            super(api, auth, mutedUserList, profileManager, ircClient, logChatToConsole);
        }
    }
    private static class MockChatAuth implements ChatAuth {
        @Override public String getSignature() { return SignatureVerifier.generateSignature(); }
        @Override public UUID getUUID() { return TEST_UUID; }
        @Override public String getHash() { return TEST_HASH; }
        @Override public @Nullable JWebToken getSessionToken() { throw new UnsupportedOperationException("Unable to mock auth. Yet."); }
    }
    private static class MockWebEngine implements WebEngine {
        @Override public EngineRequest newRequest() { throw new UnsupportedOperationException("Unable to mock web requests. Yet."); }
        @Override public EngineResponse execute(EngineRequest request) { throw new UnsupportedOperationException("Unable to mock web requests. Yet."); }
    }
    private static class MockMutedUserList extends MutedUserList {
        public MockMutedUserList(Set<String> mutedUsers) {
            super(null, mutedUsers);
        }
        @Override protected void save() { }
    }
    private static class MockIrcClient implements IrcClient {
        @Override public void start() { throw new UnsupportedOperationException("Unable to mock irc client. Yet."); }
        @Override public void stop() { throw new UnsupportedOperationException("Unable to mock irc client. Yet."); }
        @Override public IrcState getState() { return IrcState.DISCONNECTED; }
        @Override public Profile getUserProfile() { return MOCK_PROFILE_MANAGER.getOwnProfile(); }
        @Override @Nullable public IrcUser getUser(Profile profile) { return null; }
        @Override @Nullable public IrcChannel getPrimaryChannel() { return null; }
        @Override @Nullable public IrcChannel getChannel(String name) { return null; }
        @Override public Collection<IrcChannel> getChannels() { return new ArrayList<>(); }
        @Override public void addChannelListener(ChannelListener listener) { }
        @Override public void removeChannelListener(ChannelListener listener) { }
    }
    // @formatter:on

    private static class MockProfileManager extends ProfileManager {

        public MockProfileManager() {
            super(TEST_HASH);
        }

        @Override
        public ChatState getChatState() {
            return MOCK_CHAT_STATE;
        }

        @Override
        public Profile lookupProfile(String hash) {
            return lookupProfileStale(hash);
        }
    }
}
