package net.creeperhost.minetogether.module.multiplayer.sort;

import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.Comparator;

public class ServerNameComparator implements Comparator<ServerSelectionList.OnlineServerEntry>
{
    public static final ServerNameComparator INSTANCE = new ServerNameComparator();

    private ServerNameComparator() {}

    @Override
    public int compare(ServerSelectionList.OnlineServerEntry o1, ServerSelectionList.OnlineServerEntry o2)
    {
        String str1 = o1.getServerData().name;
        String str2 = o2.getServerData().name;
        int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
        if (res == 0)
        {
            res = str1.compareTo(str2);
        }
        return res;
    }
}
