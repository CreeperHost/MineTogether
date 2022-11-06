package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.irc.*;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.event.EventSubscriberListener;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.event.SubscribeEvent;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.request.IRCServerListRequest;
import net.creeperhost.minetogether.lib.chat.request.IRCServerListResponse;
import net.creeperhost.minetogether.lib.chat.util.HashLength;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.delay.StaticReadonlyDelay;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.managers.SequentialListenerManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.creeperhost.minetogether.MineTogether.API;

/**
 * Created by covers1624 on 29/6/22.
 */
public class PircBotClient implements IrcClient {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ChatState chatState;
    private final String nick;
    private final String realName;
    @Nullable
    private Thread clientThread;
    @Nullable
    private PircBotX client;
    @Nullable
    private IRCServerListResponse serverDetails;

    private final Map<Profile, PircBotUser> users = new HashMap<>();
    private final Map<String, PircBotChannel> channels = new HashMap<>();
    private final List<ChannelListener> channelListeners = new LinkedList<>();

    // TODO wire in RECONNECTING state.
    private IrcState state = IrcState.DISCONNECTED;

    public PircBotClient(ChatState chatState, String realName) {
        this.chatState = chatState;
        this.nick = "MT" + HashLength.MEDIUM.format(chatState.auth.getHash());
        this.realName = realName;
    }

    private void startClient() {
        clientThread = new Thread(() -> {
            LOGGER.debug("Starting Pircbotx MineTogether thread.");
            try {
                ApiClientResponse<IRCServerListResponse> response = API.execute(new IRCServerListRequest());
                serverDetails = response.apiResponse();
                LOGGER.info("Starting IRC. Channel: {}", serverDetails.getChannel());
                EventSubscriberListener eventListener = new EventSubscriberListener();
                eventListener.addListener(this);
                Configuration config = new Configuration.Builder()
                        .setName(nick)
                        .setRealName(realName)
                        .setLogin("MineTogether")
                        .setListenerManager(SequentialListenerManager.newDefault()
                                .addListenerSequential(eventListener)
                        )
                        .setSnapshotsEnabled(false)
                        .setAutoReconnect(true)
                        .setAutoReconnectAttempts(-1)
                        .setAutoReconnectDelay(new StaticReadonlyDelay(TimeUnit.SECONDS.toMillis(5)))
                        .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(30))
                        .setEncoding(StandardCharsets.UTF_8)
                        .addAutoJoinChannel(serverDetails.getChannel())
                        .addServer(serverDetails.getServer().getAddress(), serverDetails.getServer().getPort())
                        .buildConfiguration();
                client = new PircBotX(config);
                client.startBot();
            } catch (IOException | IrcException ex) {
                state = IrcState.CRASHED;
                LOGGER.error("Unrecoverable error occurred with IRC client.", ex);
            }
            client = null;
            LOGGER.info("Exiting Pircbotx MineTogether thread.");
        });
        clientThread.setName("MineTogether IRC Client");
        clientThread.setDaemon(true);
        clientThread.start();
    }

    @Override
    public void start() {
        if (!chatState.auth.isOnline()) {
            LOGGER.info("Refusing to start IRCClient. User is in offline mode.");
            return;
        }
        if (state == IrcState.DISCONNECTED || state == IrcState.CRASHED) {
            LOGGER.info("Starting MineTogether IRCClient.");
            state = IrcState.CONNECTING;
            startClient();
        }
    }

    @Override
    public void stop() {
        if (client != null) {
            LOGGER.info("Stopping MineTogether IRCClient.");
            client.stopBotReconnect();

            // TODO this needs a better solution.
            try {
                client.sendIRC().quitServer();
            } catch (Throwable ex) {
                LOGGER.error("Failed to send quit message..", ex);
            }
        }
    }

    @Override
    public IrcState getState() {
        return state;
    }

    @Override
    public Profile getUserProfile() {
        return chatState.profileManager.getOwnProfile();
    }

    @Nullable
    @Override
    public IrcUser getUser(Profile profile) {
        synchronized (users) {
            return users.get(profile);
        }
    }

    private PircBotUser computeUser(User ircUser) {
        Profile profile = chatState.profileManager.lookupProfileStale(ircUser.getNick());
        synchronized (users) {
            return users.computeIfAbsent(profile, p -> new PircBotUser(client, chatState, p));
        }
    }

    @Nullable
    @Override
    public PircBotChannel getPrimaryChannel() {
        if (serverDetails == null) return null;
        return getChannel(serverDetails.getChannel());
    }

    @Nullable
    @Override
    public PircBotChannel getChannel(String name) {
        return channels.get(name);
    }

    @Override
    public Collection<IrcChannel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    @Override
    public void addChannelListener(ChannelListener listener) {
        channelListeners.add(listener);
    }

    @Override
    public void removeChannelListener(ChannelListener listener) {
        channelListeners.remove(listener);
    }

    @SubscribeEvent
    private void onConnectEvent(ConnectEvent event) {
        // TODO TEMPORARY HACK Until the BNC/IRC respond with 004 nick correctly.
        try {
            Method method = PircBotX.class.getDeclaredMethod("setNick", String.class);
            method.setAccessible(true);
            method.invoke(client, nick);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    private void onNickInUse(NickAlreadyInUseEvent event) {
        LOGGER.error("Nickname already in use.");
        state = IrcState.CRASHED; // TODO, try to reconnect a bit later.
        client.stopBotReconnect();
        client.sendIRC().quitServer();
    }

    @SubscribeEvent
    private void onConnected(ConnectEvent event) {
        LOGGER.info("Connected to MineTogether IRC.");
        state = IrcState.VERIFYING;
    }

    @SubscribeEvent
    private void onDisconnected(DisconnectEvent event) {
        LOGGER.info("Disconnected from MineTogether IRC.");
        if (state != IrcState.BANNED) {
            state = IrcState.DISCONNECTED;
        }
        for (PircBotChannel channel : channels.values()) {
            channel.bindChannel(null);
        }
    }

    @SubscribeEvent
    private void onJoin(JoinEvent event) {
        User ircUser = event.getUser();
        if (ircUser == null) return; // oookay..
        // Only if we are in the 'main' channel.
        if (!event.getChannel().getName().equals(serverDetails.getChannel())) return;

        userSeen(ircUser);
    }

    @SubscribeEvent
    private void onQuit(QuitEvent event) {
        User ircUser = event.getUser();
        if (ircUser == null) return; // oookay..

        PircBotUser user = computeUser(ircUser);
        if (user.getProfile() != getUserProfile()) {
            // Only unbind if it's not us
            user.bindIrcUser(null);
            chatState.profileManager.onUserOffline(user.getProfile());
        }
    }

    @SubscribeEvent
    private void onKicked(KickEvent event) {
        User ircUser = event.getUser();
        if (ircUser == null) return; // oookay..
        // Only if we are in the 'main' channel.
        if (!event.getChannel().getName().equals(serverDetails.getChannel())) return;

        PircBotUser user = computeUser(ircUser);
        if (user.getProfile() != getUserProfile()) {
            // Only unbind if it's not us
            user.bindIrcUser(null);
        }
    }

    @SubscribeEvent
    private void onMessage(MessageEvent event) {
        User ircUser = event.getUser();
        Profile sender = chatState.profileManager.lookupProfile(ircUser.getNick());
        PircBotChannel channel = channels.get(event.getChannel().getName());
        if (channel != null) {
            channel.addMessage(Instant.ofEpochMilli(event.getTimestamp()), sender, event.getMessage());
        }

        if (chatState.logChatToConsole) {
            LOGGER.info("{}: {} | {}", event.getChannel().getName(), sender.getDisplayName(), event.getMessage());
        }

        if (channel.getName().equals(serverDetails.getChannel()) && !sender.newProfileWhoDis) {
            sender.newProfileWhoDis = true;
            ircUser.send().whois();
        }
    }

    @SubscribeEvent
    private void onPrivateMessage(PrivateMessageEvent event) {
        User ircUser = event.getUser();
        Profile sender = chatState.profileManager.lookupProfile(ircUser.getNick());
        IrcUser user = getUser(sender);
        if (user != null) {
            ((AbstractChannel) user.getChannel()).addMessage(Instant.ofEpochMilli(event.getTimestamp()), sender, event.getMessage());
        }

        if (chatState.logChatToConsole) {
            LOGGER.info("{}: {}", sender.getDisplayName(), event.getMessage());
        }

        if (!sender.newProfileWhoDis) {
            sender.newProfileWhoDis = true;
            ircUser.send().whois();
        }
    }

    @SubscribeEvent
    private void onNotice(NoticeEvent event) {
        String nick = event.getUser() != null ? event.getUser().getNick() : null;
        String cName = event.getChannel() != null ? event.getChannel().getName() : null;

        PircBotChannel channel;
        if (cName != null) {
            channel = channels.get(cName);
        } else {
            channel = getPrimaryChannel();
        }
        if (channel != null) {
            channel.addNoticeMessage(Instant.ofEpochMilli(event.getTimestamp()), event.getMessage());
        }

        if (chatState.logChatToConsole) {
            LOGGER.info("{}: System {} | {}", cName, nick, event.getMessage());
        }
    }

    @SubscribeEvent
    private void onJoinEvent(JoinEvent event) {
        User user = event.getUser();
        Channel ircChannel = event.getChannel();
        if (user.getNick().equals(nick)) {
            if (chatState.logChatToConsole) {
                LOGGER.info("Join channel: " + event.getChannel().getName());
            }
            PircBotChannel channel = channels.computeIfAbsent(ircChannel.getName(), e -> new PircBotChannel(chatState, e));
            channel.bindChannel(ircChannel);

            for (ChannelListener listener : channelListeners) {
                listener.channelJoin(channel);
            }
        }
    }

    @SubscribeEvent
    private void onUserList(UserListEvent event) {
        if (!event.getChannel().getName().equals(serverDetails.getChannel())) return;

        for (User u : event.getUsers()) {
            userSeen(u);
        }
    }

    private void userSeen(User u) {
        PircBotUser user = computeUser(u);
        user.bindIrcUser(u);

        Profile profile = user.getProfile();
        profile.unbanned(); // Assume if they are here, they aren't banned.
        profile.markStale();
        profile.setPack(u.getRealName());
        chatState.profileManager.onUserOnline(profile);
    }

    @SubscribeEvent
    private void onVoiceEvent(VoiceEvent event) {
        User user = event.getRecipient();
        if (user == null) return;
        Profile target = chatState.profileManager.lookupProfile(user.getNick());
        if (target != chatState.profileManager.getOwnProfile()) return;

        if (event.hasVoice()) {
            state = IrcState.CONNECTED;
        }
    }

    @SubscribeEvent
    private void onWhois(WhoisEvent event) {
        Profile profile = chatState.profileManager.lookupProfile(event.getNick());
        if (profile == chatState.profileManager.getOwnProfile()) return;

        profile.setPack(event.getRealname());
    }

    @SubscribeEvent
    private void onBannedEvent(SetChannelBanEvent event) {
        Profile target = chatState.profileManager.lookupProfile(event.getBanHostmask().getNick());

        target.banned();
        if (target == chatState.profileManager.getOwnProfile()) {
            state = IrcState.BANNED;
            stop();
        }
    }

    @SubscribeEvent
    private void onUnbannedEvent(RemoveChannelBanEvent event) {
        Profile target = chatState.profileManager.lookupProfile(event.getHostmask().getNick());

        target.unbanned();
    }

    @SubscribeEvent
    private void onCTCPEvent(UnknownCTCPEvent event) {
        User user = event.getUser();
        String request = event.getRequest();
        String[] split = request.split(" ", 2);
        if (chatState.logChatToConsole) {
            LOGGER.info("CTCP Request: {}", request);
        }
        switch (split[0]) {
            case "FRIENDREQ" -> {
                // System CTCP or non MT user.
                if (user == null || !user.getNick().startsWith("MT")) break;
                Profile from = chatState.profileManager.lookupProfile(user.getNick());

                String[] split2 = split[1].split(" ", 2);
                if (split2.length < 2) break;

                chatState.profileManager.onIncomingFriendRequest(from, split2[0], split2[1]);
            }
            case "FRIENDACC" -> {
                // System CTCP or non MT user.
                if (user == null || !user.getNick().startsWith("MT")) break;
                Profile source = chatState.profileManager.lookupProfile(user.getNick());

                String[] split2 = split[1].split(" ", 2);
                if (split2.length < 2) break;

                chatState.profileManager.onFriendRequestAccepted(source, split2[0], split2[1]);
            }
            case "SERVERID" -> { }
            case "VERIFY" -> {
                if (user == null || user.getNick().startsWith("MT")) {
                    break;
                }
                // TODO when id returns null, IRC needs to disconnect.
                //      Ideally we should do this before attempting to connect.
                String id = chatState.auth.beginMojangAuth();
                LOGGER.info("Verifying with: " + id);
                event.respond(String.format("VERIFY %s:%s:%s", chatState.auth.getSignature(), chatState.auth.getUUID(), id));
            }
            default -> {
                LOGGER.warn("Unknown CTCP Request from user {}: {}", user, request);
            }
        }
    }
}
