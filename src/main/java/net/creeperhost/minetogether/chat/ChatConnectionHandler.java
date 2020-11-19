package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.DebugHandler;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.common.RegexValidator;
import net.creeperhost.minetogether.data.Profile;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.util.Format;

import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatConnectionHandler {

    public static final ChatConnectionHandler INSTANCE = new ChatConnectionHandler();
    public long timeout = 0;
    public String banReason = "";
    public DebugHandler debugHandler = new DebugHandler();
    public Logger logger = LogManager.getLogger();
    public AtomicReference<CompletableFuture> chatFuture = new AtomicReference<>();

    public synchronized void setup(String nickIn, String realNameIn, boolean onlineIn, IHost _host) {
        ChatHandler.online = onlineIn;
        ChatHandler.realName = realNameIn;
        ChatHandler.initedString = nickIn;
        ChatHandler.host = _host;
        ChatHandler.nick = nickIn;
        ChatHandler.IRC_SERVER = ChatUtil.getIRCServerDetails();
        ChatHandler.CHANNEL = ChatHandler.online ? ChatHandler.IRC_SERVER.channel : "#SuperSpecialPirateClub";
        ChatHandler.host.updateChatChannel();
        ChatHandler.tries.set(0);
    }

    public synchronized void connect()
    {
        if (!canConnect())
            return;

        ChatHandler.client = null;
        ChatHandler.isInitting.set(true);

        ChatHandler.messages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        CompletableFuture tmp = chatFuture.get();
        if((tmp != null) && (!tmp.isDone())) tmp.cancel(true);
        chatFuture.set(CompletableFuture.runAsync(() ->
        { // start in thread as can hold up the UI thread for some reason.
            synchronized (INSTANCE) {
                Client.Builder mineTogether = Client.builder().nick(ChatHandler.nick).realName(ChatHandler.realName).user("MineTogether");
                mineTogether.server().host(ChatHandler.IRC_SERVER.address).port(ChatHandler.IRC_SERVER.port).secure(ChatHandler.IRC_SERVER.ssl);
                mineTogether.listeners().exception(e -> {
                    if(debugHandler.isDebug) e.printStackTrace();
                }); // no-op
                mineTogether.listeners().input(s ->
                {
//                    CreeperHost.logger.error(TextFormatting.RED + s);

                    if(debugHandler.isDebug) logger.error("INPUT " + s);
                    if(s.contains(" :Nickname is already in use") && s.contains("433"))
                    {
                        ChatHandler.reconnectTimer.set(30000);
//                        ChatHandler.addStatusMessage("You appear to be connected elsewhere delaying reconnect for 30 seconds");
                    }
                    else if(s.contains("PRIVMSG"))
                    {
                        if(s.contains("#"))
                        {
                            CompletableFuture.runAsync(() ->
                            {
                                Pattern pattern = Pattern.compile(".*(MT[A-Za-z0-9]{28}).*PRIVMSG.*(\\#\\w+) \\:(.*)");
                                Matcher matcher = pattern.matcher(s);
                                if (matcher.matches())
                                {
                                    String name = matcher.group(1);
                                    String channel = matcher.group(2);
                                    String message = matcher.group(3);
                                    ChatHandler.addMessageToChat(channel, name, Format.stripAll(message));
                                    /*if(channel.equalsIgnoreCase(ChatHandler.CHANNEL))
                                    {
                                        ChatHandler.addMessageToChat(ChatHandler.CHANNEL, name, Format.stripAll(message));
                                    }*/
                                }
                            }, CreeperHost.chatMessageExecutor);
                        }
                        else
                        {
                            CompletableFuture.runAsync(() ->
                            {
                                Pattern pattern = Pattern.compile("\\:(\\w+).*PRIVMSG.*\\:\\x01(.*)\\x01");
                                Matcher matcher = pattern.matcher(s);
                                if(matcher.matches())
                                {
                                    String name = matcher.group(1);
                                    String message = matcher.group(2);

                                    ChatHandler.Listener.onCTCP(name, message);
                                }
                            }, CreeperHost.chatMessageExecutor);
                        }
                    } else if(s.contains("NOTICE"))
                    {
                        CompletableFuture.runAsync(() ->
                        {
                            Pattern pattern = Pattern.compile("\\:(\\w+).*NOTICE.* \\:(.*)");
                            Matcher matcher = pattern.matcher(s);
                            if(matcher.matches())
                            {
                                String name = matcher.group(1);
                                String message = matcher.group(2);
                                boolean op = !name.startsWith("MT");

                                if (op)
                                {
                                    //Channel Message
                                    if (s.contains(ChatHandler.CHANNEL))
                                    {
                                        ChatHandler.Listener.onChannelNotice(name, message);
                                    }
                                    else if(s.contains(ChatHandler.nick))
                                    {
                                        ChatHandler.Listener.onNotice(name, message);
                                    }
                                }
                            }
                        }, CreeperHost.chatMessageExecutor);
                    }
                    else if(s.contains("MODE"))
                    {
                        CompletableFuture.runAsync(() ->
                        {
                            Pattern pattern = Pattern.compile("\\:(\\w+).*MODE.*\\#\\w+ (.)b (MT[a-zA-Z0-9]{28}).*");
                            Matcher matcher = pattern.matcher(s);
                            if(matcher.matches())
                            {
                                String nick = matcher.group(3);
                                String modify = matcher.group(2);
                                if(modify.equals("+"))
                                {
                                    ChatHandler.Listener.onUserBanned(nick);
                                }
                                if(modify.equals("-"))
                                {
                                    Profile profile = ChatHandler.knownUsers.findByNick(nick);
                                    profile.setBanned(false);
                                    ChatHandler.knownUsers.update(profile);
                                }
                            }
                        }, CreeperHost.ircEventExecutor);
                    }
                    //TODO we might need this for something
//                    else if(s.contains("QUIT") || s.contains("LEAVE") || s.contains("PART"))
//                    {
//                        Pattern pattern = Pattern.compile("\\:(MT\\w{28})!");
//                        Matcher matcher = pattern.matcher(s);
//                        if(matcher.matches())
//                        {
//                            String name = matcher.group(1);
//
//                        }
//                        CreeperHost.logger.error(TextFormatting.DARK_PURPLE + s);
//                    }
                });
                mineTogether.listeners().output(s ->
                {
                    if(debugHandler.isDebug) logger.error("OUTPUT " + s);
                });
                if (ChatHandler.client != null) return; // hopefully prevent multiples
                ChatHandler.client = mineTogether.buildAndConnect();

                ((Client.WithManagement) ChatHandler.client).getActorTracker().setQueryChannelInformation(true);
                ChatHandler.client.getEventManager().registerEventListener(new ChatHandler.Listener());
                ChatHandler.client.addChannel(ChatHandler.CHANNEL);
                if(ChatHandler.client.getChannel(ChatHandler.CHANNEL).isPresent())
                {
                    ChatHandler.isInChannel.set(true);
                }
                ChatHandler.inited.set(true);
                ChatHandler.isInitting.set(false);
            }
        }));
    }

    public synchronized void disconnect()
    {
        if(ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.DISCONNECTED)
        {
            ChatHandler.client.shutdown("Disconnecting.");
            ChatHandler.client = null;
            ChatHandler.connectionStatus = ChatHandler.ConnectionStatus.DISCONNECTED;
            if(debugHandler.isDebug) logger.error("Force disconnect was called");
        }
    }

    public boolean canConnect()
    {
        if(CreeperHost.profile.get().isBanned()) return false;
        if(timeout > System.currentTimeMillis()) return false;
        if(!ChatHandler.connectionStatus.equals(ChatHandler.ConnectionStatus.DISCONNECTED)) return false;
        if(!Config.getInstance().isChatEnabled()) return false;

        return true;
    }

    public void nextConnectAllow(int timeout) {
        this.timeout = timeout;
    }
}
