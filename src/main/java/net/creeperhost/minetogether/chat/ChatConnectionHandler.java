package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.common.IHost;
import org.kitteh.irc.client.library.Client;

import java.util.TreeMap;

public class ChatConnectionHandler
{
    public static final ChatConnectionHandler INSTANCE = new ChatConnectionHandler();
    public long timeout = 0;
    public boolean banned;
    public String banReason = "";
    
    public synchronized void setup(String nickIn, String realNameIn, boolean onlineIn, IHost _host)
    {
        ChatHandler.online = onlineIn;
        ChatHandler.realName = realNameIn;
        ChatHandler.initedString = nickIn;
        ChatHandler.host = _host;
        ChatHandler.nick = nickIn;
        ChatHandler.CHANNEL = ChatHandler.online ? ChatHandler.IRC_SERVER.channel : "#SuperSpecialPirateClub";
        ChatHandler.host.updateChatChannel();
        ChatHandler.badwordsFormat = ChatUtil.getAllowedCharactersRegex();
        ChatHandler.IRC_SERVER = ChatUtil.getIRCServerDetails();
        ChatHandler.badwords = ChatUtil.getBadWords();
        ChatHandler.tries = 0;
    }
    
    public synchronized void connect()
    {
        if (!canConnect())
            return;
        
        ChatHandler.client = null;
        ChatHandler.isInitting = true;
        
        ChatHandler.messages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        new Thread(() ->
        { // start in thread as can hold up the UI thread for some reason.
            synchronized (INSTANCE)
            {
                Client.Builder mineTogether = Client.builder().nick(ChatHandler.nick).realName(ChatHandler.realName).user("MineTogether");
                mineTogether.server().host(ChatHandler.IRC_SERVER.address).port(ChatHandler.IRC_SERVER.port).secure(ChatHandler.IRC_SERVER.ssl);
                mineTogether.listeners().exception(e -> {}); // no-op
                if (ChatHandler.client != null) return; // hopefully prevent multiples
                ChatHandler.client = mineTogether.buildAndConnect();
                
                ((Client.WithManagement) ChatHandler.client).getActorTracker().setQueryChannelInformation(true);
                ChatHandler.client.getEventManager().registerEventListener(new ChatHandler.Listener());
                ChatHandler.client.addChannel(ChatHandler.CHANNEL);
                ChatHandler.inited = true;
                ChatHandler.isInitting = false;
            }
        }).start();
    }
    
    public synchronized void disconnect()
    {
        ChatHandler.client.shutdown("Disconnecting.");
        ChatHandler.client = null;
        ChatHandler.connectionStatus = ChatHandler.ConnectionStatus.DISCONNECTED;
    }
    
    public boolean canConnect()
    {
        return !banned && timeout < System.currentTimeMillis() || !ChatHandler.connectionStatus.equals(ChatHandler.ConnectionStatus.DISCONNECTED) || ChatHandler.inited || ChatHandler.isInitting;
    }
    
    public void nextConnectAllow(int timeout)
    {
        this.timeout = timeout;
    }
}
