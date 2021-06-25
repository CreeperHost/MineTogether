package net.creeperhost.minetogetherlib.chat;

import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.chat.irc.IRCServer;
import net.creeperhost.minetogetherlib.chat.irc.IrcHandler;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
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
    public static TreeMap<String, LimitedSizeQueue<Message>> messages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static boolean online = false;
    public static AtomicBoolean isInitting = new AtomicBoolean(false);
    public static AtomicInteger tries = new AtomicInteger(0);
    static AtomicBoolean inited = new AtomicBoolean(false);
    public static String initedString = null;
    public static String nick;
    static String realName;
    public static AtomicBoolean isInChannel = new AtomicBoolean(false);
    public static Logger logger = LogManager.getLogger();
    private static int serverId = -1;
    public static AtomicInteger reconnectTimer = new AtomicInteger(10000);
    public static boolean rebuildChat = false;
    public static AtomicReference<List<String>> backupBan = new AtomicReference<>(new ArrayList<>());
    public static CompletableFuture isBannedFuture;
    public static String currentParty = "";
    public static boolean hasParty = false;
    public static Profile pendingPartyInvite;
    public static IChatListener iChatListener;

    public static void init(String nickIn, String realNameIn, IChatListener iChatListenerIn, boolean onlineIn)
    {
        ChatHandler.iChatListener = iChatListenerIn;
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
            init(initedString, realName, iChatListener, online);
        }
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

        ChatModule.sendMessage(ChatFormatter.formatLine(messagePair));
        newMessages.put(target, Boolean.TRUE);
    }
    
    public static void addStatusMessage(String message)
    {
        addMessageToChat(CHANNEL, "System", message);
    }
    
    public static HashMap<String, String> friends = new HashMap<>();

    public static ArrayList<String> autocompleteNames = new ArrayList<>();

    public static String getNameForUser(String nick)
    {
        return "";//host.getNameForUser(nick);
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

    public static void createPartyChannel(String owner)
    {
        IrcHandler.joinChannel("#" + owner);
        IrcHandler.sendString("MODE #" + owner + " +i", true);
        ChatHandler.hasParty = true;
        ChatHandler.currentParty = "#" + owner;
    }

    public static void leaveChannel(String channel)
    {
        if(!hasParty) return;
        IrcHandler.partChannel(channel);
        ChatHandler.hasParty = false;
        ChatHandler.currentParty = "";
    }

    public static void sendPartyInvite(String user, String owner)
    {
        if(!hasParty) createPartyChannel(owner);
        IrcHandler.sendString("INVITE " + user + " #" + owner, true);
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

    public static void onPartyInvite(Profile profile)
    {
        pendingPartyInvite = profile;
        if(iChatListener != null) iChatListener.onPartyInvite(profile);
    }

    public static void acceptPartyInvite(Profile profile)
    {
        IrcHandler.joinChannel("#" + profile.getMediumHash());
        IrcHandler.sendString("MODE #" + profile.getMediumHash() + " +i", true);
        ChatHandler.hasParty = true;
        ChatHandler.currentParty = "#" + profile.getMediumHash();
        profile.setPartyMember(true);
        KnownUsers.update(profile);
        pendingPartyInvite = null;
    }

    public static void startCleanThread()
    {
        CompletableFuture.runAsync(() ->
        {
            while (true)
            {
                KnownUsers.clean();
                try
                {
                    Thread.sleep(30000);
                } catch (InterruptedException e) { }
            }
        }, MineTogetherChat.profileExecutor);
    }
    
    public static void leaveGroupChat()
    {
        leaveChannel(currentParty);
        ChatHandler.hasParty = false;
        ChatHandler.currentParty = "";
    }

    public static String getPartyOwner()
    {
        return currentParty.replace("#", "");
    }

    public static boolean isPartyOwner()
    {
        return getPartyOwner().equals(MineTogetherChat.profile.get().getMediumHash());
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
                        String serverID = getServerId();
                        if (serverID == null) return;
                        IrcHandler.sendCTCPMessage(user, "VERIFY", MineTogetherChat.INSTANCE.signature + ":" + MineTogetherChat.INSTANCE.uuid + ":" + serverID);
                    }
            }
        }, MineTogetherChat.ircEventExecutor);
    }

    private static String getServerId()
    {
        if(iChatListener != null) return iChatListener.onServerIdRequest();
        return String.valueOf(serverId);
    }


    public static void onUserBanned(String nick)
    {
        CompletableFuture.runAsync(() ->
        {
            if (nick.equalsIgnoreCase(ChatHandler.nick)) {
                // it be us
                IrcHandler.stop(true);
                MineTogetherChat.profile.getAndUpdate(profile1 ->
                {
                    profile1.setBanned(true);
                    if (ChatHandler.isBannedFuture != null && !ChatHandler.isBannedFuture.isDone())
                        ChatHandler.isBannedFuture.cancel(true);
                    ChatHandler.isBannedFuture = CompletableFuture.runAsync(() ->
                    {
                        while (MineTogetherChat.profile.get().isBanned()) {
                            ChatCallbacks.isBanned(MineTogetherChat.INSTANCE.uuid);
                            try {
                                Thread.sleep(60000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return profile1;
                });
            } else
            {
                ChatModule.hasNewMessage = true;
                Profile profile = KnownUsers.findByNick(nick);
                if (profile == null)
                {
                    //Banned on their first message? Oops.
                    profile = KnownUsers.add(nick);
                }
                if (profile != null)
                {
                    profile.setBanned(true);
                    KnownUsers.update(profile);
                }
                backupBan.getAndUpdate((bans) -> {
                    bans.add(nick);
                    return bans;
                });
            }
        }, MineTogetherChat.ircEventExecutor);
    }
}
