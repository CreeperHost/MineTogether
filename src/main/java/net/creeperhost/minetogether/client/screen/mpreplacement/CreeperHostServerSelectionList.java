package net.creeperhost.minetogether.client.screen.mpreplacement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

import java.util.List;

/**
 * Created by Aaron on 19/05/2017.
 */
public class CreeperHostServerSelectionList extends ServerList
{
    private List<ServerData> ourList;
    private MultiplayerScreen ourParent;
    
    public CreeperHostServerSelectionList(MultiplayerScreen ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn);
        ourParent = ownerIn;
    }

//    @Override
//    public void updateOnlineServers(ServerList p_148195_1_)
//    {
//        super.updateOnlineServers(p_148195_1_);
//        ourList.add(new CreeperHostEntry(ourParent, new ServerData("", "127.0.0.1", false), true));
//    }
    
    public void replaceList(List list)
    {
        if (ourList == list)
            return;
        ourList = list;
//        ourList.add(new CreeperHostEntry(ourParent, new ServerData("", "127.0.0.1", false), true));
    }
//
//    @Override
//    public void func_214287_a(ServerSelectionList.Entry selectedSlotIndexIn) {
//        if (selectedSlotIndexIn == ourList.size() - 1)
//            selectedSlotIndexIn--;
//        super.setSelectedSlotIndex(selectedSlotIndexIn);
//    }
}
