package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.util.LimitedSizeQueue;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.command.WhoisCommand;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.WhoisData;
import org.kitteh.irc.client.library.element.mode.ChannelMode;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.element.mode.ModeStatus;
import org.kitteh.irc.client.library.event.channel.*;
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionClosedEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEndedEvent;
import org.kitteh.irc.client.library.event.user.*;
import org.kitteh.irc.client.library.util.Format;

import java.util.*;
import java.util.stream.Collectors;

public class ChatHandler
{
    public static final Object ircLock = new Object();
    public static TreeMap<String, Boolean> newMessages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static ChatUtil.IRCServer IRC_SERVER;
    public static String CHANNEL = "#MineTogether";
    public static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    public static HashMap<String, String> curseSync = new HashMap<>();

    public static TreeMap<String, LimitedSizeQueue<Message>> messages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static Client client = null;
    static IHost host;
    static boolean online = false;
    public static boolean isInitting = false;
    public static int tries = 0;
    static boolean inited = false;
    public static List<String> badwords;
    public static String badwordsFormat;
    public static String currentGroup = "";
    public static String initedString = null;
    static String nick;
    static String realName;
    public static PrivateChat privateChatList = null;
    public static PrivateChat privateChatInvite = null;
    public static boolean hasGroup = false;

    public static void init(String nickIn, String realNameIn, boolean onlineIn, IHost _host)
    {
        ChatConnectionHandler.INSTANCE.setup(nickIn, realNameIn, onlineIn, _host);
        ChatConnectionHandler.INSTANCE.connect();
    }

    public static void reInit()
    {
        if (!isInitting && host != null && initedString != null && realName != null)
        {
            inited = false;
            init(initedString, realName, online, host);
        }
    }

    private static void addMessageToChat(String target, String user, String message)
    {
        LimitedSizeQueue<Message> tempQueue = messages.get(target);
        if (tempQueue == null)
        {
            messages.put(target, tempQueue = new LimitedSizeQueue<>(150));
        }

        Message messagePair = new Message(System.currentTimeMillis(), user, message);
        tempQueue.add(messagePair);
        host.messageReceived(target, messagePair);
        newMessages.put(target, Boolean.TRUE);
    }

    public static void addStatusMessage(String message)
    {
        addMessageToChat(CHANNEL, "System", message);
    }

    public static HashMap<String, String> friends = new HashMap<>();
    public static HashMap<String, String> anonUsers = new HashMap<>();
    public static HashMap<String, String> anonUsersReverse = new HashMap<>();
    public static ArrayList<String> autocompleteNames = new ArrayList<>();
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
        for (Friend friend : friendsCall)
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

        for (Map.Entry<String, String> friend : friends.entrySet())
        {
            if (!oldFriends.containsKey(friend.getKey()))
            {
                host.friendEvent(friend.getKey(), false);
            }
        }
    }

    public static void sendMessage(String currentTarget, String text)
    {
        String nick;
        if (ChatHandler.isOnline())
        {
            nick = client.getNick();
            if (currentTarget.equals(CHANNEL))
            {
                client.getChannel(CHANNEL).get().sendMessage(text);
            } else if (privateChatList != null && currentTarget.equals(privateChatList.channelname))
            {
                try
                {
                    client.addChannel(privateChatList.getChannelname()); //Just to make sure the user is connected to the channel
                    client.getChannel(privateChatList.getChannelname()).get().sendMessage(text);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } else if (client.getChannel(CHANNEL).get().getUser(currentTarget).isPresent())
            {
                client.getChannel(CHANNEL).get().getUser(currentTarget).get().sendMessage(text);
            } else
            {
                updateFriends(client.getChannel(CHANNEL).get().getNicknames());
                return;
            }
        } else
        {
            text = "Message not sent as not connected.";
            nick = "System";
        }

        synchronized (ircLock)
        {
            addMessageToChat(currentTarget, nick, text);
        }
    }

    public static void sendFriendRequest(String target, String desiredName)
    {
        Optional<User> userOpt = client.getChannel(CHANNEL).get().getUser(target);
        if (userOpt.isPresent())
        {
            User user = userOpt.get();
            user.sendCtcpMessage("FRIENDREQ " + host.getFriendCode() + " " + desiredName);
        } else
        {
            addMessageToChat(CHANNEL, "System", "User is not online.");
        }
    }

    public static void sendChannelInvite(String target, String owner)
    {
        Optional<User> userOpt = client.getChannel(CHANNEL).get().getUser(target);
        String channelName = "#" + owner;

        if (!userOpt.isPresent())
            userOpt = client.getChannel(CHANNEL).get().getUser(target + "`");
        if (userOpt.isPresent())
        {
            if (privateChatList != null && !privateChatList.getChannelname().equals(channelName))
                closePrivateChat();
            User user = userOpt.get();
            client.addChannel(channelName);
            Optional<Channel> channel = client.getChannel(channelName);
            channel.ifPresent(channel1 ->
            {
                channel1.commands().mode().add(ModeStatus.Action.ADD, client.getServerInfo().getChannelMode('i').get()).execute();
            });
            ChatHandler.hasGroup = true;
            ChatHandler.currentGroup = channelName;
            privateChatList = new PrivateChat(channelName, owner);
            String inviteStr = "INVITE " + user.getNick() + " " + channelName;
            client.sendRawLine(inviteStr);
        } else
        {
            addMessageToChat(CHANNEL, "System", "User is not online.");
        }
    }

    public static boolean isOnline()
    {
        return connectionStatus == ConnectionStatus.CONNECTED && client.getChannel(CHANNEL).isPresent();
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

    public static void acceptPrivateChatInvite(PrivateChat invite)
    {
        if (hasGroup)
            closePrivateChat();
        privateChatList = invite;
        client.addChannel(invite.getChannelname());
        currentGroup = invite.getChannelname();
        hasGroup = true;
        privateChatInvite = null;
    }

    public static void closePrivateChat()
    {
        String channelName = privateChatList.getChannelname();
        Optional<Channel> channel = client.getChannel(channelName);
        channel.ifPresent(channel1 -> channel1.part("My buddy left :("));
        privateChatList = null;
        ChatHandler.hasGroup = false;
    }

    public static List<String> getOnlineUsers()
    {
        return client.getChannel(CHANNEL).map(channel1 -> channel1.getUsers().stream().map(User::getNick).collect(Collectors.toList())).orElse(new ArrayList<>());
    }

    public static class Listener
    {
        @Handler
        public void onChannnelLeave(ChannelKickEvent event)
        {
            if (!event.getTarget().getNick().equals(client.getNick()))
                return;

            String reason = "Kicked - " + event.getMessage();
            event.getChannel().join();
            synchronized (ircLock)
            {
                if (tries >= 4)
                {
                    client.shutdown();
                    addMessageToChat(event.getChannel().getName(), "System", "Unable to rejoin chat. Disconnected from server");
                }
                addMessageToChat(event.getChannel().getName(), "System", Format.stripAll("Removed from chat (Reason: " + reason + "). Rejoining"));
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
            else if ((event instanceof ClientConnectionClosedEvent) && ((ClientConnectionClosedEvent) event).getLastMessage().isPresent())
                cause = ((ClientConnectionClosedEvent) event).getLastMessage().get();

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
            if (client.isUser(event.getUser()))
            {
                synchronized (ircLock)
                {
                    connectionStatus = ConnectionStatus.CONNECTED;
                    Channel channel = event.getAffectedChannel().get();
                    if (channel.getName().toUpperCase().equals("#" + client.getNick().toUpperCase()))
                    {
                        channel.commands().mode().add(ModeStatus.Action.ADD, client.getServerInfo().getChannelMode('i').get()).execute();
                    }
                    addMessageToChat(event.getChannel().getName(), "System", Format.stripAll("Chat joined"));
                }
            }
            updateFriends(event.getChannel().getNicknames());
        }

        @Handler
        public void onChannelLeave(ChannelPartEvent event)
        {
            try
            {
                String channelName = event.getAffectedChannel().get().getName();
                if (channelName.equals(CHANNEL))
                {
                    String friendNick = event.getUser().getNick();
                    friends.remove(friendNick);
                } else
                {
                    if (privateChatList != null && channelName.equals(privateChatList.channelname))
                    {
                        if (privateChatList.owner.equals(event.getUser().getNick()))
                        {
                            host.closeGroupChat();
                            // TODO: make sure the chat closes too
                        }
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Handler
        public void onUserQuit(UserQuitEvent event)
        {
            String friendNick = event.getUser().getNick();
            friends.remove(friendNick);
            if (privateChatList != null && privateChatList.owner.equals(friendNick))
            {
                host.closeGroupChat();
                // TODO: make sure the chat closes too
            }
        }

        @Handler
        public void onChannelMessage(ChannelMessageEvent event)
        {
            User user = event.getActor();
            String message = event.getMessage();
            try
            {
                if (!curseSync.containsKey(user.getNick()))
                {
                    if (user.getRealName().isPresent())
                        curseSync.put(
                                user.getNick(),
                                user.getRealName().get());
                    else
                        doWhois(user);
                }
            } catch (Throwable t)
            {
                t.printStackTrace();
            }

            synchronized (ircLock)
            {
                addMessageToChat(event.getChannel().getName(), user.getNick(), Format.stripAll(message));
            }

            updateFriends(client.getChannel(CHANNEL).get().getNicknames());
        }

        private WhoisCommand whoisCommand = null;

        private void doWhois(User user)
        {
            if (whoisCommand == null || whoisCommand.getClient() != client)
                whoisCommand = new WhoisCommand(client);

            whoisCommand.target(user.getNick()).execute();
        }

        @Handler
        public void onWhoisReturn(WhoisEvent event)
        {
            WhoisData whoisData = event.getWhoisData();
            if (whoisData.getRealName().isPresent())
                curseSync.put(whoisData.getNick(), whoisData.getRealName().get());

        }

        @Handler
        public void onChannelNotice(ChannelNoticeEvent event)
        {
            Optional<SortedSet<ChannelUserMode>> userModes = event.getChannel().getUserModes(event.getActor());
            if (userModes.isPresent())
            {
                SortedSet<ChannelUserMode> channelUserModes = userModes.get();
                boolean valid = false;
                for (ChannelUserMode mode : channelUserModes)
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
                    for (ChannelUserMode mode : channelUserModes)
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

                if (split[0].equals("FRIENDREQ"))
                {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < split.length; i++)
                    {
                        builder.append(split[i]).append(" ");
                    }

                    String chatMessage = builder.toString().trim();

                    addMessageToChat(CHANNEL, "FR:" + event.getActor().getNick(), chatMessage);
                } else if (split[0].equals("FRIENDACC"))
                {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 2; i < split.length; i++)
                    {
                        builder.append(split[i]).append(" ");
                    }

                    host.acceptFriend(split[1], builder.toString().trim());
                    addMessageToChat(CHANNEL, "FA:" + event.getActor().getNick(), builder.toString().trim());

                }
            }
        }

        @Handler
        public void onInviteReceived(ChannelInviteEvent event)
        {
            String actorName = event.getActor().getName();
            actorName = actorName.substring(0, actorName.indexOf("!"));
            privateChatInvite = new PrivateChat(event.getChannel().getName(), actorName);
        }

        @Handler
        public void onNickRejected(NickRejectedEvent event)
        {
            host.messageReceived(ChatHandler.CHANNEL, new Message(System.currentTimeMillis(), "System", "Couldn't connect as your nick is in use. Waiting 5 minutes and trying again."));
            ChatConnectionHandler.INSTANCE.nextConnectAllow(600);
            ChatConnectionHandler.INSTANCE.disconnect();
        }

        @Handler
        public void onUserBanned(ChannelModeEvent event)
        {
            List<ModeStatus<ChannelMode>> b = event.getStatusList().getByMode('b');
            b.forEach(mode -> mode.getParameter().ifPresent(param ->
            {
                String nick = param.split("!")[0];
                if (nick.toLowerCase().equals(ChatHandler.nick.toLowerCase()))
                {
                    // it be us
                    ChatConnectionHandler.INSTANCE.disconnect();
                    ChatConnectionHandler.INSTANCE.banned = true;
                    host.messageReceived(ChatHandler.CHANNEL, new Message(System.currentTimeMillis(), "System", "You were banned from the chat."));
                }
                host.userBanned(nick);
            }));
        }
    }

    public static void createChannel(String name)
    {
        client.addChannel(name);
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
