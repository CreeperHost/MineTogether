package net.creeperhost.minetogether.minetogetherlib.serverlists;

public class Invite
{
    public final String name;
    public final String ip;
    public final int project;
    public final Server server;
    public String by;
    
    public Invite(Server server, int project, String by)
    {
        this.name = server.displayName;
        this.ip = server.host;
        this.project = project;
        this.by = by;
        this.server = server;
    }
}
