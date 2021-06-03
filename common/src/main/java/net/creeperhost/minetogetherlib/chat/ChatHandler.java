package net.creeperhost.minetogetherlib.chat;

import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogetherlib.chat.data.Friend;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.PrivateChat;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.chat.irc.IRCServer;
import net.creeperhost.minetogetherlib.chat.irc.IrcHandler;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChatHandler
{
    public static final Object ircLock = new Object();
    public static TreeMap<String, Boolean> newMessages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static IRCServer IRC_SERVER;
    public static String CHANNEL = "#MineTogether";
    public static ChatConnectionStatus connectionStatus = ChatConnectionStatus.DISCONNECTED;
    public static HashMap<String, String> curseSync = new HashMap<>();
    public static TreeMap<String, LimitedSizeQueue<Message>> messages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
    public static AtomicInteger reconnectTimer = new AtomicInteger(10000);
    public static boolean rebuildChat = false;
    public static AtomicReference<List<String>> backupBan = new AtomicReference<>(new ArrayList<>());
    public static CompletableFuture isBannedFuture;

    public static void init(String nickIn, String realNameIn, boolean onlineIn)
    {
        ChatConnectionHandler.INSTANCE.setup(nickIn, realNameIn, onlineIn);
        ChatConnectionHandler.INSTANCE.connect();
        startCleanThread();
    }
    
    public static void reInit()
    {
        if (!isInitting.get() && initedString != null && realName != null)
        {
            inited.set(false);
            connectionStatus = ChatConnectionStatus.CONNECTING;
            init(initedString, realName, online);
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
//        host.messageReceived(target, messagePair);
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
        return "";//host.getNameForUser(nick);
    }
    
    public static void updateFriends(List<String> users)
    {
        List<Friend> friendsCall = ChatCallbacks.getFriendsList(false, MineTogetherClient.getUUID());
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
                }, MineTogetherChat.profileExecutor);

//                host.friendEvent(friend.getKey(), false);
            }
        }
    }
    
    public static void sendMessage(String currentTarget, String text)
    {
        if(IrcHandler.sendMessage(currentTarget, text)) {
            synchronized (ircLock) {
                addMessageToChat(currentTarget, nick, text);
            }
        }
    }

    public static void sendFriendRequest(String target, String desiredName)
    {
        IrcHandler.sendCTCPMessagePrivate(target, "FRIENDREQ", MineTogetherChat.profile.get().getFriendCode() + " " + desiredName);
    }
    
    public static void sendChannelInvite(String target, String owner)
    {
        privateChatList = null;

        if(privateChatList == null)
        {
            IrcHandler.joinChannel("#" + owner);
            IrcHandler.sendString("MODE #" + owner + " +i", true);
            privateChatList = new PrivateChat("#" + owner, owner);
            ChatHandler.hasGroup = true;
            ChatHandler.currentGroup = "#" + owner;
        }
        IrcHandler.sendString("INVITE " + target + " #" + owner, true);
    }

    public static boolean isOnline()
    {
        return connectionStatus == ChatConnectionStatus.VERIFIED;
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
        IrcHandler.sendCTCPMessagePrivate(chatInternalName, "FRIENDACC", MineTogetherChat.profile.get().getFriendCode() + " " + desiredName);
        addMessageToChat(CHANNEL, "System", "Friend request accepted.");
    }
    
    public static void acceptPrivateChatInvite(PrivateChat invite)
    {
        if (hasGroup) closePrivateChat();
        privateChatList = invite;
        IrcHandler.joinChannel(invite.getChannelname());
        currentGroup = invite.getChannelname();
        hasGroup = true;
        privateChatInvite = null;
    }

    public static void startCleanThread()
    {
        CompletableFuture.runAsync(() ->
        {
            while (true)
            {
                knownUsers.clean();
                try
                {
                    Thread.sleep(30000);
                } catch (InterruptedException e) { }
            }
        }, MineTogetherChat.profileExecutor);
    }
    
    public static void closePrivateChat()
    {
        IrcHandler.sendString("PART " + privateChatList.getChannelname(), true);
        privateChatList = null;
        ChatHandler.hasGroup = false;
    }

    public static void onChannelNotice(String user, String message)
    {
        CompletableFuture.runAsync(() ->
        {
            synchronized (ircLock)
            {
                addMessageToChat(CHANNEL, "System", message);
            }
        }, MineTogetherChat.chatMessageExecutor);
    }

    public static void onNotice(String name, String message)
    {
        CompletableFuture.runAsync(() ->
        {
            synchronized (ircLock)
            {
                addMessageToChat(CHANNEL, "System", message);
            }
        }, MineTogetherChat.chatMessageExecutor);
    }

    public static void onCTCP(String user, String message)
    {
        CompletableFuture.runAsync(() -> {
//                MineTogether.instance.getLogger().error(user + " " + message);
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

//                    host.acceptFriend(split[1], builder.toString().trim());
                    addMessageToChat(CHANNEL, "FA:" + user, builder.toString().trim());
                    break;
                }
                case "SERVERID":
                    IrcHandler.sendCTCPMessage(user, "SERVERID", getServerId());
                    break;
                case "VERIFY":
                    if (!user.startsWith("MT")) {
                        String serverID = MineTogetherChat.INSTANCE.serverID;
                        if (serverID == null) return;
                        IrcHandler.sendCTCPMessage(user, "VERIFY", MineTogetherChat.INSTANCE.signature + ":" + MineTogetherChat.INSTANCE.uuid + ":" + serverID);
                    }
            }
        }, MineTogetherChat.ircEventExecutor);
    }

    private static String getServerId() {
        return String.valueOf(serverId);
    }


    public static void onUserBanned(String nick)
    {
        CompletableFuture.runAsync(() ->
        {
            if (nick.equalsIgnoreCase(ChatHandler.nick)) {
                // it be us
//                ChatHandler.host.userBanned(nick);
                IrcHandler.stop(true);
                MineTogetherChat.profile.getAndUpdate(profile1 ->
                {
                    profile1.setBanned(true);
                    if (ChatHandler.isBannedFuture != null && !ChatHandler.isBannedFuture.isDone())
                        ChatHandler.isBannedFuture.cancel(true);
                    ChatHandler.isBannedFuture = CompletableFuture.runAsync(() ->
                    {
                        while (MineTogetherChat.profile.get().isBanned()) {
//                            ChatCallbacks.isBanned();
                            try {
                                Thread.sleep(60000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return profile1;
                });
//                host.messageReceived(ChatHandler.CHANNEL, new Message(System.currentTimeMillis(), "System", "You were banned from the chat."));
//
//                host.userBanned(nick);
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
        }, MineTogetherChat.ircEventExecutor);
    }
}
