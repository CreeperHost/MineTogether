package net.creeperhost.minetogether.gui.serverlist.data;

public class Invite
{

    public final String name;
    public final String ip;
    public final int project;
    public String by;
    public final Server server;

    public Invite(Server server, int project, String by)
    {
        this.name = server.displayName;
        this.ip = server.host;
        this.project = project;
        this.by = by;
        this.server = server;
    }
}
