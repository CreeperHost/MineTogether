package net.creeperhost.minetogether.module.multiplayer.sort;

import net.creeperhost.minetogether.module.multiplayer.data.PublicServerEntry;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.Comparator;

public class UptimeComparator implements Comparator<ServerSelectionList.Entry>
{
    public static final UptimeComparator INSTANCE = new UptimeComparator();

    private UptimeComparator()
    {
    }

    @Override
    public int compare(ServerSelectionList.Entry o1, ServerSelectionList.Entry o2)
    {
        PublicServerEntry p1 = (PublicServerEntry) o1;
        PublicServerEntry p2 = (PublicServerEntry) o2;

        return p1.getServerData().server.uptime > p2.getServerData().server.uptime ? -1
                : p1.getServerData().server.uptime < p2.getServerData().server.uptime ? 1
                : 0;
    }
}
