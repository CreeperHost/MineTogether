package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.DebugHandler;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.common.IHost;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.irc.IrcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ChatConnectionHandler
{
    public static final ChatConnectionHandler INSTANCE = new ChatConnectionHandler();
    public long timeout = 0;
    public boolean banned;
    public String banReason = "";
    public DebugHandler debugHandler = new DebugHandler();
    public Logger logger = LogManager.getLogger();

    public synchronized void setup(String nickIn, String realNameIn, boolean onlineIn, IHost _host)
    {
        ChatHandler.online = onlineIn;
        ChatHandler.realName = realNameIn;
        ChatHandler.initedString = nickIn;
        ChatHandler.host = _host;
        ChatHandler.nick = nickIn;
        ChatHandler.IRC_SERVER = ChatUtil.getIRCServerDetails();
        ChatHandler.CHANNEL = ChatHandler.online ? ChatHandler.IRC_SERVER.channel : "#SuperSpecialPirateClub";
        ChatHandler.host.updateChatChannel();
        ChatHandler.tries.set(0);
        banned = MineTogether.instance.isBanned.get();
    }

    public synchronized void connect()
    {
        IrcHandler.start(ChatUtil.getIRCServerDetails());
    }
    
    public boolean canConnect()
    {
        return !banned && timeout < System.currentTimeMillis() || !ChatHandler.connectionStatus.equals(ChatHandler.ConnectionStatus.DISCONNECTED) || ChatHandler.inited.get() || ChatHandler.isInitting.get() || Config.getInstance().isChatEnabled();
    }
    
    public void nextConnectAllow(int timeout)
    {
        this.timeout = timeout;
    }
}
