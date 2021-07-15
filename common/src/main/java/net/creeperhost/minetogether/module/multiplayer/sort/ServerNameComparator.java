package net.creeperhost.minetogether.module.multiplayer.sort;

import net.creeperhost.minetogether.module.multiplayer.data.PublicServerEntry;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.Comparator;

public class ServerNameComparator implements Comparator<ServerSelectionList.Entry>
{
    public static final ServerNameComparator INSTANCE = new ServerNameComparator();

    private ServerNameComparator()
    {
    }

    @Override
    public int compare(ServerSelectionList.Entry o1, ServerSelectionList.Entry o2)
    {
        PublicServerEntry p1 = (PublicServerEntry) o1;
        PublicServerEntry p2 = (PublicServerEntry) o2;

        String str1 = p1.getServerData().name;
        String str2 = p2.getServerData().name;

        int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
        if (res == 0)
        {
            res = str1.compareTo(str2);
        }
        return res;
    }
}
