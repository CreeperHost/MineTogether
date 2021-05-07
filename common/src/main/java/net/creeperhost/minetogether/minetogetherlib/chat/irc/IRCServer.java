package net.creeperhost.minetogether.minetogetherlib.chat.irc;

public class IRCServer
{
    public final String address;
    public final int port;
    public final boolean ssl;
    public final String channel;

    public IRCServer(String address, int port, boolean ssl, String channel)
    {
        this.address = address;
        this.port = port;
        this.ssl = ssl;
        this.channel = channel;
    }
}
