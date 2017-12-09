package net.creeperhost.creeperhost.gui.serverlist;

public class Invite
{

  public final String name;
  public final String ip;
  public final int port;
  public final int project;
  public String by;
  public Invite(String name, String ip, int port, int project, String by)
  {
    this.name = name;
    this.ip = ip;
    this.port = port;
    this.project = project;
    this.by = by;
  }
}
