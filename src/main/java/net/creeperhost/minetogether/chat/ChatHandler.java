package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.common.LimitedSizeQueue;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.serverlist.data.Friend;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent;
import org.kitteh.irc.client.library.event.channel.UnexpectedChannelLeaveViaKickEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectionClosedEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectionEndedEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectionEstablishedEvent;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.helper.UnexpectedChannelLeaveEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
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
    static String initedString = null;

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
        }
        client = Client.builder().nick(nick).realName("https://minetogether.io").user("MineTogether").serverHost(IRC_SERVER.address).serverPort(IRC_SERVER.port).secure(IRC_SERVER.ssl).exceptionListener((Exception exception) -> {} /* noop */).buildAndConnect();
        client.getEventManager().registerEventListener(new Listener());
        client.addChannel(CHANNEL);

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
        tempQueue.add(new Pair<>(user, message));
        newMessages.put(target, new Boolean(true));
    }

    public static HashMap<String, String> friends = new HashMap<>();
    private static HashMap<String, String> anonUsers = new HashMap<>();
    private static Random random = new Random();

    public static String getNameForUser(String nick)
    {
        if(friends.containsKey(nick))
        {
            return friends.get(nick);
        }
        if (nick.startsWith("MT"))
        {
            if(anonUsers.containsKey(nick))
            {
                return anonUsers.get(nick);
            } else {
                String anonymousNick = "Anonymous" + random.nextInt(999);
                while (anonUsers.containsValue(anonymousNick))
                {
                    anonymousNick = "Anonymous" + random.nextInt(999);
                }
                anonUsers.put(nick, anonymousNick);
                return anonymousNick;
            }
        }
        return null;
    }

    private static void updateFriends(List<User> users)
    {
        List<Friend> friendsCall = host.getFriends();
        HashMap<String, String> oldFriends = friends;
        friends = new HashMap<>();
        for(Friend friend: friendsCall)
        {
            String friendCode = "MT" + friend.getCode().substring(0, 15);
            for (User user: users)
            {
                if (user.getNick().equals(friendCode))
                    friends.put(friendCode, friend.getName());
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
            updateFriends(client.getChannel(CHANNEL).get().getUsers());
            return;
        }

        synchronized (ircLock)
        {
            addMessageToChat(currentTarget, client.getNick(), text);
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

            updateFriends(event.getChannel().getUsers());
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

            updateFriends(client.getChannel(CHANNEL).get().getUsers());
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
