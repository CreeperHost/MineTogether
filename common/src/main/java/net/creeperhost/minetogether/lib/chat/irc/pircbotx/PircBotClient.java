package net.creeperhost.minetogether.lib.chat.irc.pircbotx;

import net.creeperhost.minetogether.lib.chat.ChatAuth;
import net.creeperhost.minetogether.lib.chat.MutedUserList;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.event.EventSubscriberListener;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.event.SubscribeEvent;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.lib.chat.request.IRCServerListResponse;
import net.creeperhost.minetogether.lib.chat.util.HashLength;
import net.creeperhost.minetogether.lib.web.ApiClient;
import org.apache.commons.lang3.StringUtils;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 29/6/22.
 */
public class PircBotClient implements IrcClient {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ChatAuth auth;
    private final MutedUserList mutedUserList;
    private final ApiClient api;
    private final String nick;
    private final IRCServerListResponse serverDetails;
    private final ProfileManager profileManager;
    private final Profile profile;
    private final PircBotX client;
    private final Thread clientThread;

    private final Map<String, PircBotChannel> channels = new HashMap<>();
    private final List<ChannelListener> channelListeners = new LinkedList<>();

    // TODO wire in RECONNECTING state.
    private IrcState state = IrcState.DISCONNECTED;

    public PircBotClient(ChatAuth auth, MutedUserList mutedUserList, ApiClient api, IRCServerListResponse serverDetails, String realName) {
        this.auth = auth;
        this.mutedUserList = mutedUserList;
        this.api = api;
        this.nick = "MT" + HashLength.MEDIUM.format(auth.getHash());
        this.serverDetails = serverDetails;
        // TODO move this to a Property passed into PircBotClient, it should be unrelated to the IRC implementation.
        profileManager = new ProfileManager(api, mutedUserList);
        profile = profileManager.lookupProfile(auth.getHash());

        // TODO make EventSubscriberListener fire all events on a specific executor.
        EventSubscriberListener eventListener = new EventSubscriberListener();
        eventListener.addListener(this);
        Configuration config = new Configuration.Builder()
                .setName(nick)
                .setRealName(realName)
                .setLogin("MineTogether")
                .addListener(eventListener)
                .addAutoJoinChannel(serverDetails.getChannel())
                .addServer(serverDetails.getServer().getAddress(), serverDetails.getServer().getPort())
                .setAutoReconnect(true)
                .setAutoReconnectAttempts(-1)
                .setAutoReconnectDelay(new StaticReadonlyDelay(TimeUnit.SECONDS.toMillis(5)))
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(30))
                .buildConfiguration();
        client = new PircBotX(config);
        clientThread = new Thread(() -> {
            LOGGER.debug("Starting Pircbotx MineTogether thread.");
            try {
                client.startBot();
            } catch (IOException | IrcException ex) {
                state = IrcState.CRASHED;
                LOGGER.error("Unrecoverable error occurred with IRC client");
            }
            LOGGER.info("Exiting Pircbotx MineTogether thread.");
        });
        clientThread.setName("MineTogether IRC Client");
        clientThread.setDaemon(true);
    }

    @Override
    public void connect() throws IllegalStateException {
        switch (state) {
            case CONNECTING -> throw new IllegalStateException("Client is already connecting");
            case RECONNECTING -> throw new IllegalStateException("Client is already reconnecting.");
            case CONNECTED -> throw new IllegalStateException("Already connected.");
            case CRASHED -> throw new IllegalStateException("Client has crashed, construct new instance.");
        }
        state = IrcState.CONNECTING;
        clientThread.start();
    }

    @Override
    public IrcState getState() {
        return state;
    }

    @Override
    public Profile getUserProfile() {
        return profile;
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Nullable
    @Override
    public PircBotChannel getPrimaryChannel() {
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
        state = IrcState.CONNECTED;
    }

    @SubscribeEvent
    private void onDisconnected(DisconnectEvent event) {
        LOGGER.info("Disconnected from MineTogether IRC.");
        state = IrcState.DISCONNECTED;
        for (PircBotChannel channel : channels.values()) {
            channel.bindChannel(null);
        }
    }

    @SubscribeEvent
    private void onMessage(MessageEvent event) {
        Profile sender = profileManager.lookupProfile(event.getUser().getNick());
        PircBotChannel channel = channels.get(event.getChannel().getName());
        if (channel != null) {
            channel.addMessage(Instant.ofEpochMilli(event.getTimestamp()), sender, event.getMessage());
        }

        LOGGER.info("{}: {} | {}", event.getChannel().getName(), sender.getDisplayName(), event.getMessage());
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

        LOGGER.info("{}: System {} | {}", cName, nick, event.getMessage());
    }

    @SubscribeEvent
    private void onJoinEvent(JoinEvent event) {
        User user = event.getUser();
        Channel ircChannel = event.getChannel();
        if (user.getNick().equals(nick)) {
            PircBotChannel channel = channels.computeIfAbsent(ircChannel.getName(), e -> new PircBotChannel(this, e));
            channel.bindChannel(ircChannel);

            for (ChannelListener listener : channelListeners) {
                listener.channelJoin(channel);
            }
        }
    }

    @SubscribeEvent
    private void onUserModeChange(UserModeEvent event) {
        User user = event.getUser();
        if (user != null) {
            boolean hasBanMode = StringUtils.containsAny(event.getMode(), 'b');
            if (!hasBanMode) return;

            // Who is the recipient of this mode change, us or another user.
            Profile target = user.getNick().equals(nick) ? profile : profileManager.lookupProfile(user.getNick());

            // Apply ban/unban
            if (event.getMode().charAt(0) == '-') {
                target.unbanned();
            } else {
                assert event.getMode().charAt(0) == '+';
                target.banned();
            }
        }
    }

    @SubscribeEvent
    private void onCTCPEvent(UnknownCTCPEvent event) {
        User user = event.getUser();
        String request = event.getRequest();
        String[] split = request.split(" ", 2);
        LOGGER.info("CTCP Request: {}", request);
        switch (split[0]) {
            case "FRIENDREQ": {
                break;
            }
            case "FRIENDACC": {
                break;
            }
            case "SERVERID": {
                break;
            }
            case "VERIFY": {
                if (user == null || user.getNick().startsWith("MT")) {
                    break;
                }
                // TODO when id returns null, IRC needs to disconnect.
                //      Ideally we should do this before attempting to connect.
                String id = auth.beginMojangAuth();
                LOGGER.info("Verifying with: " + id);
                event.respond(String.format("VERIFY %s:%s:%s", auth.getSignature(), auth.getUUID(), id));
                break;
            }
            default: {
                LOGGER.warn("Unknown CTCP Request from user {}: {}", user, request);
            }
        }
    }
}
