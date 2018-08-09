package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.common.LimitedSizeQueue;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.serverlist.data.Friend;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.channel.*;
import org.kitteh.irc.client.library.event.client.ClientConnectionClosedEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectionEndedEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectionEstablishedEvent;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.helper.UnexpectedChannelLeaveEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateNoticeEvent;
import org.kitteh.irc.client.library.event.user.UserQuitEvent;
import org.kitteh.irc.client.library.util.Format;

import java.util.*;
import java.util.function.Consumer;

public class ChatHandler
{
    public static final Object ircLock = new Object();
    public static HashMap<String, Boolean> newMessages = new HashMap<>();
    private static ChatUtil.IRCServer IRC_SERVER;
    public static String CHANNEL = "#MineTogether";
    public static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public static HashMap<String, LimitedSizeQueue<Pair<String, String>>> messages = null;
    private static Client client = null;
    private static IHost host;
    public static int tries = 0;
    private static boolean inited = false;
    public static List<String> badwords;
    public static String badwordsFormat;
    public static String initedString = null;

    public static void init(String nick, IHost _host)
    {
        if (inited) return;
        initedString = nick;
        badwords = ChatUtil.getBadWords();
        badwordsFormat = ChatUtil.getAllowedCharactersRegex();
        IRC_SERVER = ChatUtil.getIRCServerDetails();
        CHANNEL = IRC_SERVER.channel;
        host = _host;
        tries = 0;

        synchronized (ircLock)
        {
            messages = new HashMap<>();
            new Thread(() ->
            { // start in thread as can hold up the UI thread for some reason.
                client = Client.builder().nick(nick).realName("https://minetogether.io").user("MineTogether").serverHost(IRC_SERVER.address).serverPort(IRC_SERVER.port).secure(IRC_SERVER.ssl).exceptionListener((Exception exception) ->
                {
                } /* noop */).buildAndConnect();
                ((Client.WithManagement) client).getActorTracker().setQueryChannelInformation(false); // no longer does a WHO;
                client.getEventManager().registerEventListener(new Listener());
                client.addChannel(CHANNEL);
            }).start();
        }


        inited = true;
    }

    public static void reInit()
    {
        inited = false;
        init(initedString, host);
    }

    private static void addMessageToChat(String target, String user, String message)
    {
        LimitedSizeQueue<Pair<String, String>> tempQueue = messages.get(target);
        if (tempQueue == null)
        {
            messages.put(target, tempQueue = new LimitedSizeQueue<>(150));
        }
        Pair messagePair = new Pair<>(user, message);
        tempQueue.add(messagePair);
        host.messageReceived(target, messagePair);
        newMessages.put(target, new Boolean(true));
    }

    public static void addStatusMessage(String message)
    {
        addMessageToChat(CHANNEL, "System", message);
    }

    public static HashMap<String, String> friends = new HashMap<>();
    public static HashMap<String, String> anonUsers = new HashMap<>();
    public static HashMap<String, String> anonUsersReverse = new HashMap<>();
    public static Random random = new Random();

    public static String getNameForUser(String nick)
    {
        return host.getNameForUser(nick);
    }

    private static void updateFriends(List<String> users)
    {
        List<Friend> friendsCall = host.getFriends();
        HashMap<String, String> oldFriends = friends;
        friends = new HashMap<>();
        for(Friend friend: friendsCall)
        {
            if (friend.isAccepted()) // why did I never do this before?
            {
                String friendCode = "MT" + friend.getCode().substring(0, 15);
                for (String user : users)
                {
                    if (user.equals(friendCode))
                        friends.put(friendCode, friend.getName());
                }
            }
        }

        for(Map.Entry<String, String> friend : friends.entrySet())
        {
            if(!oldFriends.containsKey(friend.getKey()))
            {
                host.friendEvent(friend.getKey(), false);
            }
        }
    }

    public static void sendMessage(String currentTarget, String text)
    {
        if (currentTarget.equals(CHANNEL))
            client.getChannel(CHANNEL).get().sendMessage(text);
        else if(client.getChannel(CHANNEL).get().getUser(currentTarget).isPresent())
            client.getChannel(CHANNEL).get().getUser(currentTarget).get().sendMessage(text);
        else
        {
            updateFriends(client.getChannel(CHANNEL).get().getNicknames());
            return;
        }

        synchronized (ircLock)
        {
            addMessageToChat(currentTarget, client.getNick(), text);
        }
    }

    public static void sendFriendRequest(String target, String desiredName)
    {
        Optional<User> userOpt = client.getChannel(CHANNEL).get().getUser(target);
        if (userOpt.isPresent())
        {
            User user = userOpt.get();
            StringBuilder builder = new StringBuilder("FRIENDREQ ").append(host.getFriendCode()).append(" ").append(desiredName);
            user.sendCtcpMessage(builder.toString());
        } else {
            addMessageToChat(CHANNEL, "System", "User is not online.");
        }
    }

    public static boolean hasNewMessages(String target)
    {
        return newMessages.get(target) != null && newMessages.get(target);
    }

    public static void setMessagesRead(String target)
    {
        newMessages.put(target, false);
    }

    public static void acceptFriendRequest(String chatInternalName, String desiredName)
    {
        Optional<Channel> channelOpt = client.getChannel(CHANNEL);
        if (!channelOpt.isPresent())
            return;

        Channel channel = channelOpt.get();

        Optional<User> userOpt = channel.getUser(chatInternalName);

        if (!userOpt.isPresent())
            return;

        User user = userOpt.get();

        user.sendCtcpMessage("FRIENDACC " + host.getFriendCode() + " " + desiredName);

        addMessageToChat(CHANNEL, "System", "Friend request accepted.");
    }

    public static class Listener
    {
        @Handler
        public void onChannnelLeave(UnexpectedChannelLeaveEvent event)
        {
            String reason = "Unknown";
            if (event instanceof UnexpectedChannelLeaveViaKickEvent)
            {
                UnexpectedChannelLeaveViaKickEvent kicked = (UnexpectedChannelLeaveViaKickEvent) event;
                reason = "Kicked - " + kicked.getMessage();
            }
            event.getChannel().join();
            synchronized (ircLock)
            {
                if (tries >= 4)
                {
                    client.shutdown();
                    addMessageToChat(CHANNEL, "System", "Unable to rejoin chat. Disconnected from server");
                }
                addMessageToChat(CHANNEL, "System", Format.stripAll("Removed from chat (Reason: " + reason + "). Rejoining"));
                connectionStatus = ConnectionStatus.NOT_IN_CHANNEL;
            }
        }

        @Handler
        public void onConnected(ClientNegotiationCompleteEvent event)
        {
            tries = 0;
        }

        @Handler
        public void onQuit(ClientConnectionEndedEvent event)
        {
            String cause = "Unknown";
            if (event.getCause().isPresent())
                cause = event.getCause().get().getMessage();
            else if ((event instanceof ClientConnectionClosedEvent) && ((ClientConnectionClosedEvent)event).getLastMessage().isPresent())
                cause = ((ClientConnectionClosedEvent)event).getLastMessage().get();

            tries++;


            synchronized (ircLock)
            {
                connectionStatus = ConnectionStatus.DISCONNECTED;
                if (tries >= 5)
                {
                    event.setAttemptReconnect(false);
                    addMessageToChat(CHANNEL, "System", Format.stripAll("Disconnected (Reason: " + cause + "). Too many tries, not reconnecting"));
                    return;
                }
                addMessageToChat(CHANNEL, "System", Format.stripAll("Disconnected (Reason: " + cause + "). Reconnecting"));
                event.setReconnectionDelay(10000);
                event.setAttemptReconnect(true);
            }
        }

        @Handler
        public void onChannelJoin(ChannelJoinEvent event)
        {
            if (event.getChannel().getLowerCaseName().equals(CHANNEL.toLowerCase()) && client.isUser(event.getUser()))
            {
                synchronized (ircLock)
                {
                    connectionStatus = ConnectionStatus.CONNECTED;
                    addMessageToChat(CHANNEL, "System", Format.stripAll("Server joined"));
                }
            }

            updateFriends(event.getChannel().getNicknames());
        }

        @Handler
        public void onChannelLeave(ChannelPartEvent event)
        {
            String friendNick = event.getUser().getNick();
            if (friends.containsKey(friendNick))
            {
                friends.remove(friendNick);
            }
        }

        public void onUserQuit(UserQuitEvent event)
        {
            String friendNick = event.getUser().getNick();
            if (friends.containsKey(friendNick))
            {
                friends.remove(friendNick);
            }
        }

        @Handler
        public void onChannelMessage(ChannelMessageEvent event)
        {
            User user = event.getActor();
            String message = event.getMessage();

            synchronized (ircLock)
            {
                addMessageToChat(CHANNEL, user.getNick(), Format.stripAll(message));
            }

            updateFriends(client.getChannel(CHANNEL).get().getNicknames());
        }

        @Handler
        public void onChannelNotice(ChannelNoticeEvent event)
        {
            Optional<SortedSet<ChannelUserMode>> userModes = event.getChannel().getUserModes(event.getActor());
            if (userModes.isPresent())
            {
                SortedSet<ChannelUserMode> channelUserModes = userModes.get();
                boolean valid = false;
                for(ChannelUserMode mode: channelUserModes)
                {
                    switch (mode.getNickPrefix())
                    {
                        case '@':
                        case '~':
                            valid = true;
                    }
                }

                if (valid)
                {
                    synchronized (ircLock)
                    {
                        addMessageToChat(CHANNEL, "System", event.getMessage());
                    }
                }
            }

        }

        @Handler
        public void onNotice(PrivateNoticeEvent event)
        {
            User user = event.getActor();
            Optional<Channel> optchannel = client.getChannel(CHANNEL);
            if (optchannel.isPresent())
            {
                Channel channel = optchannel.get();
                Optional<SortedSet<ChannelUserMode>> userModesOpt = channel.getUserModes(user);
                if (userModesOpt.isPresent())
                {
                    SortedSet<ChannelUserMode> channelUserModes = userModesOpt.get();
                    boolean valid = false;
                    for(ChannelUserMode mode: channelUserModes)
                    {
                        switch (mode.getNickPrefix())
                        {
                            case '@':
                            case '~':
                                valid = true;
                        }
                    }

                    if (valid)
                    {
                        synchronized (ircLock)
                        {
                            addMessageToChat(CHANNEL, "System", event.getMessage());
                        }
                    }
                }
            }

        }

        @Handler
        public void onPrivateMessage(PrivateMessageEvent event)
        {
            String message = Format.stripAll(event.getMessage());
            String user = event.getActor().getNick();
            if (friends.containsKey(user))
            {
                synchronized (ircLock)
                {
                    LimitedSizeQueue messageQueue = messages.get(user);
                    if (messageQueue == null)
                    {
                        messages.put(user, new LimitedSizeQueue<>(150));
                    }
                    addMessageToChat(user, user, message);
                    host.friendEvent(user, true);
                }
            }
        }

        @Handler
        public void onCTCP(PrivateCtcpQueryEvent event)
        {
            if (event.isToClient())
            {
                String message = event.getMessage();
                String[] split = message.split(" ");
                if (split.length < 3)
                    return;

                if (!split[0].equals("FRIENDREQ"))
                    return;

                StringBuilder builder = new StringBuilder();
                for(int i = 1; i < split.length; i++)
                {
                    builder.append(split[i]).append(" ");
                }

                String chatMessage = builder.toString().trim();

                addMessageToChat(CHANNEL, "FR:" + event.getActor().getNick(), chatMessage);
            }
        }
    }

    public enum ConnectionStatus
    {
        CONNECTED("Connected", "GREEN"),
        CONNECTING("Connecting", "GOLD"),
        DISCONNECTED("Disconnected", "RED"),
        NOT_IN_CHANNEL("Not in channel", "RED");

        public final String display;
        public final String colour;

        ConnectionStatus(String display, String colour)
        {
            this.display = display;
            this.colour = colour;
        }
    }
}
