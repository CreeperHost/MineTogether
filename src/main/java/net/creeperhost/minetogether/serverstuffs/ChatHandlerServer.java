package net.creeperhost.minetogether.serverstuffs;

import java.util.concurrent.CompletableFuture;

public class ChatHandlerServer
{
    public String nick;
    public String realName;
    public String ircServerAddress;
    public int ircServerPort;
    public boolean ssl;

    public ChatHandlerServer(String nick, String realName, String ircServerAddress, int ircServerPort, boolean ssl)
    {
        this.nick = nick;
        this.realName = realName;
        this.ircServerAddress = ircServerAddress;
        this.ircServerPort = ircServerPort;
        this.ssl = ssl;
    }

    public void init()
    {
        CompletableFuture.runAsync(() ->
        {
//            Client.Builder mineTogether = Client.builder().nick(nick).realName(realName).user("MineTogether");
//            mineTogether.server().host(ircServerAddress).port(ircServerPort).secure(ssl);
//            if(client == null) client = mineTogether.buildAndConnect(); client.addChannel("#servers");
        });
    }

//    public void messageUser(String userNick, String message)
//    {
//        client.sendMessage(userNick, message);
//    }
}
