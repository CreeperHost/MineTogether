package net.creeperhost.minetogether.client.gui.serverlist.gui.elements;

import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiMultiplayerPublic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerList;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Aaron on 19/05/2017.
 */
public class ServerSelectionListPublic extends ServerSelectionList
{
    private static Field serverListInternetField;
    private List<ServerListEntryPublic> ourList;
    private GuiMultiplayerPublic ourParent;
    private long nextSort;
    
    public ServerSelectionListPublic(GuiMultiplayerPublic ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(ownerIn, mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        ourParent = ownerIn;
//        makeOurList();
    }

//    @Override
//    protected int getSize()
//    {
//        return super.getSize() - 1;
//    }
    
    @Override
    protected Entry getEntry(int p_getEntry_1_)
    {
        return super.getEntry(p_getEntry_1_);
    }

//    @Override
//    public GuiListExtended.IGuiListEntry getListEntry(int index)
//    {
//        return super.getListEntry(index);
//    }
    
    @Override
    public void updateOnlineServers(ServerList serverList)
    {
        ourList.clear();
        
        if (serverList instanceof ServerListPublic)
        {
            ServerListPublic pub = (ServerListPublic) serverList;
            for (int i = 0; i < pub.countServers(); ++i)
            {
//                ourList.add(new ServerListEntryPublic(ourParent, new ServerListEntryNormalPubConstructor(ourParent, pub.getServerData(i))));
            }
        }

//        sort();
    }

//    @SuppressWarnings("Duplicates")
//    public void sort(boolean resetScroll)
//    {
//        switch (ourParent.sortOrder)
//        {
//            default:
//            case RANDOM:
//                Collections.shuffle(ourList);
//                break;
//            case PLAYER:
//                Collections.sort(ourList, Server.PlayerComparator.INSTANCE);
//                break;
//            case UPTIME:
//                Collections.sort(ourList, Server.UptimeComparator.INSTANCE);
//                break;
//            case NAME:
//                Collections.sort(ourList, Server.NameComparator.INSTANCE);
//                break;
//            case LOCATION:
//                Collections.sort(ourList, Server.LocationComparator.INSTANCE);
//                break;
//            case PING:
//                Collections.sort(ourList, Server.PingComparator.INSTANCE);
//                break;
//        }
//        if (resetScroll) amountScrolled = 0;
//    }

//    public void sort()
//    {
//        sort(true);
//    }

//    private void makeOurList()
//    {
//        if (serverListInternetField == null)
//        {
//            if (serverListInternetField == null)
//            {
//                serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "serverListInternet", "field_148198_l", "");
//                serverListInternetField.setAccessible(true);
//            }
//        }
//
//        try
//        {
//            ourList = (List<ServerListEntryPublic>) serverListInternetField.get(this);
//        } catch (IllegalAccessException e)
//        {
//            MineTogether.logger.warn("Reflection to get server list failed.", e);
//        }
//    }
    
    @Override
    protected void renderBackground()
    {
        if (!ourParent.sortOrder.constant)
            return;
        // NOOP usually - we're going to use this as something that will be called regularly so we can update sorting
        long curTime = System.currentTimeMillis();
        if (nextSort <= curTime)
        {
            nextSort = curTime + 500;
//            sort(false);
        }
    }
}
