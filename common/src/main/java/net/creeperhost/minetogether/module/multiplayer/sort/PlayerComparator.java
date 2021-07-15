package net.creeperhost.minetogether.module.multiplayer.sort;

import net.creeperhost.minetogether.module.multiplayer.data.PublicServerEntry;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.Comparator;

public class PlayerComparator implements Comparator<ServerSelectionList.Entry>
{
    public static final PlayerComparator INSTANCE = new PlayerComparator();

    private PlayerComparator()
    {
    }

    @Override
    public int compare(ServerSelectionList.Entry o1, ServerSelectionList.Entry o2)
    {
        PublicServerEntry p1 = (PublicServerEntry) o1;
        PublicServerEntry p2 = (PublicServerEntry) o2;

        int o1Players = 0;
        int o2Players = 0;

        if (p1.getServerData().playerList != null)
        {
            o1Players = p1.getServerData().playerList.size();
        }
        if (p2.getServerData().playerList != null)
        {
            o2Players = p2.getServerData().playerList.size();
        }
        return o1Players > p2.getServerData().server.uptime ? -1 : o1Players < o2Players ? 1 : 0;
    }
}
