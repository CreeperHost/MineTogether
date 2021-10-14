package net.creeperhost.minetogether.module.multiplayer.sort;

import net.creeperhost.minetogether.lib.serverorder.ServerOrderCallbacks;
import net.creeperhost.minetogether.module.multiplayer.data.PublicServerEntry;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.Comparator;

public class LocationComparator implements Comparator<ServerSelectionList.Entry>
{
    public static final LocationComparator INSTANCE = new LocationComparator();

    private LocationComparator()
    {
    }

    @Override
    public int compare(ServerSelectionList.Entry o1, ServerSelectionList.Entry o2)
    {
        PublicServerEntry p1 = (PublicServerEntry) o1;
        PublicServerEntry p2 = (PublicServerEntry) o2;

        if (p1.getServerData().server.flag == null)
        {
            return 1;
        }
        else if (p2.getServerData().server.flag == null)
        {
            return -1;
        }
        else if (p1.getServerData().server.flag == p2.getServerData().server.flag)
        {
            return 1;
        }
        else if (p1.getServerData().server.flag.name().equals(ServerOrderCallbacks.getUserCountry()))
        {
            if (p2.getServerData().server.flag.name().equals(ServerOrderCallbacks.getUserCountry()))
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
        else if (p2.getServerData().server.flag.name().equals(ServerOrderCallbacks.getUserCountry()))
        {
            if (p1.getServerData().server.flag.name().equals(ServerOrderCallbacks.getUserCountry()))
            {
               return -1;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            String str1 = p1.getServerData().server.flag.name();
            String str2 = p2.getServerData().server.flag.name();
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0)
            {
                res = str1.compareTo(str2);
            }
            return res;
        }
    }
}
