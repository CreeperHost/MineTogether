package net.creeperhost.minetogether.module.multiplayer.sort;

import net.creeperhost.minetogether.module.multiplayer.data.PublicServerEntry;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.Comparator;

public class PingComparator implements Comparator<ServerSelectionList.Entry>
{
    public static final PingComparator INSTANCE = new PingComparator();

    private PingComparator()
    {
    }

    @Override
    public int compare(ServerSelectionList.Entry o1, ServerSelectionList.Entry o2)
    {
        PublicServerEntry p1 = (PublicServerEntry) o1;
        PublicServerEntry p2 = (PublicServerEntry) o2;

        if (p1.getServerData().ping == p2.getServerData().ping)
        {
            return 0;
        }
        if (p1.getServerData().ping <= 0)
        {
            return 1;
        }
        if (p2.getServerData().ping <= 0)
        {
            return -1;
        }
        return Long.compare(p1.getServerData().ping, p2.getServerData().ping);
    }
}
