package net.creeperhost.minetogether.gui.serverlist.data;

import net.creeperhost.minetogether.gui.serverlist.gui.elements.ServerListEntryPublic;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.data.EnumFlag;

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
    
    public static class NameComparator implements Comparator<ServerListEntryPublic>
    {
        public static final NameComparator INSTANCE = new NameComparator();
        
        private NameComparator()
        {
        }
        
        @Override
        public int compare(ServerListEntryPublic o1, ServerListEntryPublic o2)
        {
            String str1 = o1.getServerData().server.displayName;
            String str2 = o2.getServerData().server.displayName;
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0)
            {
                res = str1.compareTo(str2);
            }
            return res;
        }
    }
    
    public static class PlayerComparator implements Comparator<ServerListEntryPublic>
    {
        public static final PlayerComparator INSTANCE = new PlayerComparator();
        
        private PlayerComparator()
        {
        }
        
        @Override
        public int compare(ServerListEntryPublic o1, ServerListEntryPublic o2)
        {
            return o1.getServerData().server.playerCount > o2.getServerData().server.playerCount ? -1
                    : o1.getServerData().server.playerCount < o2.getServerData().server.playerCount ? 1
                    : 0;
        }
    }
    
    public static class UptimeComparator implements Comparator<ServerListEntryPublic>
    {
        public static final UptimeComparator INSTANCE = new UptimeComparator();
        
        private UptimeComparator()
        {
        }
        
        @Override
        public int compare(ServerListEntryPublic o1, ServerListEntryPublic o2)
        {
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
        public int compare(ServerListEntryPublic o1, ServerListEntryPublic o2)
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
    
    public static class PingComparator implements Comparator<ServerListEntryPublic>
    {
        public static final PingComparator INSTANCE = new PingComparator();
        
        private PingComparator()
        {
        }
        
        @Override
        public int compare(ServerListEntryPublic o1, ServerListEntryPublic o2)
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
            return o1.getServerData().pingToServer < o2.getServerData().pingToServer ? -1
                    : o1.getServerData().pingToServer > o2.getServerData().pingToServer ? 1
                    : 0;
        }
    }
}
