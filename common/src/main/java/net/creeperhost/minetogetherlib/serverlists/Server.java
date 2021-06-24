package net.creeperhost.minetogetherlib.serverlists;

public class Server
{
    public final String displayName;
    public final String host;
    public final int uptime;
    public final int playerCount;
    public final EnumFlag flag;
    public final String subdivision;
    public final String applicationURL;

    public Server(String displayName, String host, int uptime, int playerCount, EnumFlag flag, String subdivision, String applicationURL)
    {
        this.displayName = displayName;
        this.host = host;
        this.uptime = uptime;
        this.playerCount = playerCount;
        this.flag = flag;
        this.subdivision = subdivision;
        this.applicationURL = applicationURL;
    }

    @Override
    public String toString()
    {
        return "Server[" + displayName + ", " + host + ", " + uptime + ", " + playerCount + ", " + flag.name() + "]";
    }
}
