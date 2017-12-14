package net.creeperhost.minetogether.gui.serverlist;

import java.util.Comparator;

public class Server
{
  public final String displayName;
  public final String host;
  public final int uptime;
  public final int playerCount;

  public Server(String displayName, String host, int uptime, int playerCount)
  {
    this.displayName = displayName;
    this.host = host;
    this.uptime = uptime;
    this.playerCount = playerCount;
  }

  @Override
  public String toString()
  {
    return "Server[" + displayName + ", " + host + ", " + uptime + ", " + playerCount + "]";
  }

  public static class NameComparator implements Comparator<Server>
  {
    @Override
    public int compare(Server o1, Server o2) {
      String str1 = o1.displayName;
      String str2 = o2.displayName;
      int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
      if (res == 0) {
        res = str1.compareTo(str2);
      }
      return res;
    }
  }

  public static class PlayerComparator implements Comparator<Server>
  {
    @Override
    public int compare(Server o1, Server o2)
    {
      return o1.playerCount > o2.playerCount ? -1
        : o1.playerCount < o2.playerCount ? 1
        : 0;
    }
  }

  public static class UptimeComparator implements Comparator<Server>
  {
    @Override
    public int compare(Server o1, Server o2)
    {
      return o1.uptime > o2.uptime ? -1
        : o1.uptime < o2.uptime ? 1
        : 0;
    }
  }
}
