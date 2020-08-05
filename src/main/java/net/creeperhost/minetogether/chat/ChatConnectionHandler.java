package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.DebugHandler;
import net.creeperhost.minetogether.common.IHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitteh.irc.client.library.Client;

import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ChatConnectionHandler {

    public static final ChatConnectionHandler INSTANCE = new ChatConnectionHandler();
    public long timeout = 0;
    public boolean banned;
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
                    if(debugHandler.isDebug)
                        logger.error("INPUT " + s);
                    if(s.contains(" :Nickname is already in use") && s.contains("433"))
                    {
                        ChatHandler.reconnectTimer.set(30000);
//                        ChatHandler.addStatusMessage("You appear to be connected elsewhere delaying reconnect for 30 seconds");
                    }
                });
                mineTogether.listeners().output(s ->
                {
                    if(debugHandler.isDebug)
                        logger.error("OUTPUT " + s);
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
        return !banned && timeout < System.currentTimeMillis() || !ChatHandler.connectionStatus.equals(ChatHandler.ConnectionStatus.DISCONNECTED) || ChatHandler.inited.get() || ChatHandler.isInitting.get();
    }

    public void nextConnectAllow(int timeout) {
        this.timeout = timeout;
    }
}
