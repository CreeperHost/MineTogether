package net.creeperhost.minetogether.client.gui.serverlist.data;

import net.creeperhost.minetogether.data.EnumFlag;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.screen.ServerSelectionList;

import java.util.Comparator;

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
    
    public static class NameComparator implements Comparator<ServerSelectionListOurs.ServerListEntryPublic>
    {
        public static final NameComparator INSTANCE = new NameComparator();
        
        private NameComparator()
        {
        }
        
        @Override
        public int compare(ServerSelectionListOurs.ServerListEntryPublic o1, ServerSelectionListOurs.ServerListEntryPublic o2)
        {
            String str1 = o1.getServerData().serverName;
            String str2 = o2.getServerData().serverName;
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0)
            {
                res = str1.compareTo(str2);
            }
            return res;
        }
    }
    
    public static class PlayerComparator implements Comparator<ServerSelectionListOurs.ServerListEntryPublic>
    {
        public static final PlayerComparator INSTANCE = new PlayerComparator();
        
        private PlayerComparator()
        {
        }
        
        @Override
        public int compare(ServerSelectionListOurs.ServerListEntryPublic o1, ServerSelectionListOurs.ServerListEntryPublic o2)
        {
            int o1Players = 0;
            int o2Players = 0;
            
            if (o1.getServerData().playerList != null)
            {
                o1Players = o1.getServerData().playerList.length();
            }
            if (o2.getServerData().playerList != null)
            {
                o2Players = o2.getServerData().playerList.length();
            }
            return Integer.compare(o2Players, o1Players);
        }
    }
    
    public static class UptimeComparator implements Comparator<ServerSelectionListOurs.ServerListEntryPublic>
    {
        public static final UptimeComparator INSTANCE = new UptimeComparator();
        
        private UptimeComparator()
        {
        }
        
        @Override
        public int compare(ServerSelectionListOurs.ServerListEntryPublic o1, ServerSelectionListOurs.ServerListEntryPublic o2)
        {
            System.out.println(o1.getServerData().server.uptime + " " + o2.getServerData().server.uptime);
            return o1.getServerData().server.uptime > o2.getServerData().server.uptime ? -1
                    : o1.getServerData().server.uptime < o2.getServerData().server.uptime ? 1
                    : 0;
        }
    }
    
    public static class LocationComparator extends NameComparator
    {
        public static final LocationComparator INSTANCE = new LocationComparator();
        
        private LocationComparator()
        {
        }
        
        @Override
        public int compare(ServerSelectionListOurs.ServerListEntryPublic o1, ServerSelectionListOurs.ServerListEntryPublic o2)
        {
            if (o1.getServerData().server.flag == null)
            {
                return 1;
            } else if (o2.getServerData().server.flag == null)
            {
                return -1;
            } else if (o1.getServerData().server.flag == o2.getServerData().server.flag)
            {
                return super.compare(o1, o2);
            } else if (o1.getServerData().server.flag.name().equals(Callbacks.getUserCountry()))
            {
                if (o2.getServerData().server.flag.name().equals(Callbacks.getUserCountry()))
                {
                    return super.compare(o1, o2);
                } else
                {
                    return -1;
                }
            } else if (o2.getServerData().server.flag.name().equals(Callbacks.getUserCountry()))
            {
                if (o1.getServerData().server.flag.name().equals(Callbacks.getUserCountry()))
                {
                    return super.compare(o1, o2);
                } else
                {
                    return 1;
                }
            } else
            {
                String str1 = o1.getServerData().server.flag.name();
                String str2 = o2.getServerData().server.flag.name();
                int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
                if (res == 0)
                {
                    res = str1.compareTo(str2);
                }
                return res;
            }
        }
    }
    
    public static class PingComparator implements Comparator<ServerSelectionList.NormalEntry>
    {
        public static final PingComparator INSTANCE = new PingComparator();
        
        private PingComparator()
        {
        }
        
        @Override
        public int compare(ServerSelectionList.NormalEntry o1, ServerSelectionList.NormalEntry o2)
        {
            if (o1.getServerData().pingToServer == o2.getServerData().pingToServer)
            {
                return 0;
            }
            if (o1.getServerData().pingToServer <= 0)
            {
                return 1;
            }
            if (o2.getServerData().pingToServer <= 0)
            {
                return -1;
            }
            return Long.compare(o1.getServerData().pingToServer, o2.getServerData().pingToServer);
        }
    }
}
