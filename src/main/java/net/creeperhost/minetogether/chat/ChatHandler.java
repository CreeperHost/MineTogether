package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.DebugHandler;
import net.creeperhost.minetogether.KnownUsers;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.LimitedSizeQueue;
import net.engio.mbassy.listener.Handler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.kitteh.irc.client.library.event.connection.ClientConnectionEndedEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionFailedEvent;
import org.kitteh.irc.client.library.event.user.*;
import org.kitteh.irc.client.library.util.Format;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    public static Client client = null;
    static IHost host;
    static boolean online = false;
    public static AtomicBoolean isInitting = new AtomicBoolean(false);
    public static AtomicInteger tries = new AtomicInteger(0);
    static AtomicBoolean inited = new AtomicBoolean(false);
    public static String currentGroup = "";
    public static String initedString = null;
    public static String nick;
    static String realName;
    public static PrivateChat privateChatList = null;
    public static PrivateChat privateChatInvite = null;
    public static boolean hasGroup = false;
    public static AtomicBoolean isInChannel = new AtomicBoolean(false);
    public static Logger logger = LogManager.getLogger();
    private static int serverId = -1;
    public static DebugHandler debugHandler = new DebugHandler();
    public static AtomicInteger reconnectTimer = new AtomicInteger(10000);
    public static boolean rebuildChat = false;
    public static AtomicReference<List<String>> backupBan = new AtomicReference<>(new ArrayList<>());
    public static CompletableFuture isBannedFuture;

    public static void init(String nickIn, String realNameIn, boolean onlineIn, IHost _host)
    {
        ChatConnectionHandler.INSTANCE.setup(nickIn, realNameIn, onlineIn, _host);
        ChatConnectionHandler.INSTANCE.connect();
    }
    
    public static void reInit()
    {
        if (!isInitting.get() && host != null && initedString != null && realName != null)
        {
            if(debugHandler.isDebug) logger.debug("ChatHandler attempting a reconnect");
            inited.set(false);
            connectionStatus = ConnectionStatus.CONNECTING;
            init(initedString, realName, online, host);
        }
    }

    public static void setServerId(int serverIdIn) {
        serverId = serverIdIn;
    }

    public static void addMessageToChat(String target, String user, String message)
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
    public static final KnownUsers knownUsers = new KnownUsers();

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
                String friendCode = "MT" + friend.getCode().substring(0, 28);
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
                CompletableFuture.runAsync(() ->
                {
                    Profile profile = knownUsers.findByNick(friend.getKey());
                    if(profile == null) knownUsers.add(friend.getKey());
                    if(profile != null) profile.isOnline();
                }, MineTogether.profileExecutor);

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
            } else
            {
                Profile profile = knownUsers.findByNick(currentTarget);
                if(profile != null && profile.isOnline())
                {
                    client.sendMessage(currentTarget, text);
                } else {
                    updateFriends(client.getChannel(CHANNEL).get().getNicknames());
                    return;
                }
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
        return connectionStatus == ConnectionStatus.CONNECTED && client != null && client.getChannel(CHANNEL).isPresent();
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
            if(event.getAffectedChannel().get().getName().equalsIgnoreCase(CHANNEL))
            {
                Profile profile = knownUsers.findByNick(nick);
                if(profile != null)
                {
                    CompletableFuture.runAsync(() -> profile.loadProfile(), MineTogether.profileExecutor).thenRun(() -> profile.setBanned(false));
                }
            }
            updateFriends(event.getChannel().getNicknames());
        }

        private WhoisCommand whoisCommand = null;
        
        private void doWhois(User user)
        {
            if (whoisCommand == null || whoisCommand.getClient() != client)
                whoisCommand = new WhoisCommand(client);
            
            whoisCommand.target(user.getNick()).execute();
        }

        public static void onChannelNotice(String user, String message)
        {
            CompletableFuture.runAsync(() ->
            {
                synchronized (ircLock)
                {
                    addMessageToChat(CHANNEL, "System", message);
                }
            }, MineTogether.chatMessageExecutor);
        }

        public static void onNotice(String name, String message)
        {
            CompletableFuture.runAsync(() ->
            {
                synchronized (ircLock)
                {
                    addMessageToChat(CHANNEL, "System", message);
                }
            }, MineTogether.chatMessageExecutor);
        }

        public static void onCTCP(String user, String message)
        {
            CompletableFuture.runAsync(() -> {

                String[] split = message.split(" ");
                if (split.length < 1)
                    return;

                switch (split[0].trim()) {
                    case "FRIENDREQ": {
                        if (split.length < 3)
                            return;
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < split.length; i++) {
                            builder.append(split[i]).append(" ");
                        }
                        String chatMessage = builder.toString().trim();
                        addMessageToChat(CHANNEL, "FR:" + user, chatMessage);
                        break;
                    }
                    case "FRIENDACC": {
                        if (split.length < 3)
                            return;
                        StringBuilder builder = new StringBuilder();
                        for (int i = 2; i < split.length; i++) {
                            builder.append(split[i]).append(" ");
                        }

                        host.acceptFriend(split[1], builder.toString().trim());
                        addMessageToChat(CHANNEL, "FA:" + user, builder.toString().trim());
                        break;
                    }
                    case "SERVERID":
                        client.sendCtcpReply(user, "SERVERID " + getServerId());
                        break;
                    case "VERIFY":
                        if (!user.startsWith("MT")) {
                            String serverID = MineTogether.getServerIDAndVerify();
                            if (serverID == null) return;
                            client.sendCtcpReply(user, "VERIFY " + MineTogether.getSignature() + ":" + MineTogether.proxy.getUUID() + ":" + serverID);
                        }
                }
            }, MineTogether.ircEventExecutor);
        }

        private static String getServerId() {
            return String.valueOf(serverId);
        }


        public static void onUserBanned(String nick)
        {
            CompletableFuture.runAsync(() ->
            {
//                List<ModeStatus<ChannelMode>> b = event.getStatusList().getByMode('b');
//                b.forEach(mode -> mode.getParameter().ifPresent(param -> {
//                    String nick = param.split("!")[0];


                if (nick.equalsIgnoreCase(ChatHandler.nick)) {
                    // it be us
                    ChatHandler.host.userBanned(nick);
                    ChatConnectionHandler.INSTANCE.disconnect();
                    ChatHandler.killChatConnection(false);
                    MineTogether.profile.getAndUpdate(profile1 ->
                    {
                        profile1.setBanned(true);
                        if (ChatHandler.isBannedFuture != null && !ChatHandler.isBannedFuture.isDone())
                            ChatHandler.isBannedFuture.cancel(true);
                        ChatHandler.isBannedFuture = CompletableFuture.runAsync(() ->
                        {
                            while (MineTogether.profile.get().isBanned()) {
                                Callbacks.isBanned();
                                try {
                                    Thread.sleep(60000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        return profile1;
                    });
                    host.messageReceived(ChatHandler.CHANNEL, new Message(System.currentTimeMillis(), "System", "You were banned from the chat."));

                    host.userBanned(nick);
                } else {
                    Profile profile = knownUsers.findByNick(nick);
                    if (profile == null)
                    {
                        //Banned on their first message? Oops.
                        profile = knownUsers.add(nick);
                    }
                    if (profile != null)
                    {
                        profile.setBanned(true);
                        knownUsers.update(profile);
                    }
                    backupBan.getAndUpdate((bans) -> {
                        bans.add(nick);
                        return bans;
                    });
                }
            }, MineTogether.ircEventExecutor);
        }

        @Handler
        public void onNickRejected(NickRejectedEvent event)
        {
            if(event.getAttemptedNick().equalsIgnoreCase(nick))
            {
                addStatusMessage("Unable to connect to chat. Please make sure you do not have another Minecraft client open. If you have the FTBApp running, ensure that you launched Minecraft via the FTBApp.");
            }
        }
    }

    public static void requestReconnect()
    {
        logger.warn("Attempting to reconnect chat");
        killChatConnection(true);
    }

    public static void killChatConnection(boolean reconnect)
    {
        ChatHandler.client.shutdown();
        try {
            ChatHandler.addStatusMessage((reconnect) ? "Chat disconnected, Reconnecting" : "Chat has disconnected.");
            ChatHandler.connectionStatus = ConnectionStatus.DISCONNECTED;
            Thread.sleep(reconnectTimer.get());
            logger.error("Reinit being called");
            if(reconnect) {
                reInit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        NOT_IN_CHANNEL("Not in channel", "RED"),
        BANNED("Banned", "BLACK");

        public final String display;
        public final String colour;
        
        ConnectionStatus(String display, String colour)
        {
            this.display = display;
            this.colour = colour;
        }
    }
}
