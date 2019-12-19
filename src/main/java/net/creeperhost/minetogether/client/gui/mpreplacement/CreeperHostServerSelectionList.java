package net.creeperhost.minetogether.gui.mpreplacement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

import java.util.List;

/**
 * Created by Aaron on 19/05/2017.
 */
public class CreeperHostServerSelectionList extends ServerSelectionList
{
    private List<ServerListEntryNormal> ourList;
    private GuiMultiplayer ourParent;
    
    public CreeperHostServerSelectionList(GuiMultiplayer ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(ownerIn, mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        ourParent = ownerIn;
    }
    
    @Override
    public void updateOnlineServers(ServerList p_148195_1_)
    {
        super.updateOnlineServers(p_148195_1_);
        ourList.add(new CreeperHostEntry(ourParent, new ServerData("", "127.0.0.1", false), true));
    }
    
    public void replaceList(List list)
    {
        if (ourList == list)
            return;
        ourList = list;
        ourList.add(new CreeperHostEntry(ourParent, new ServerData("", "127.0.0.1", false), true));
    }

    @Override
    public void setSelectedSlotIndex(int selectedSlotIndexIn) {
        if (selectedSlotIndexIn == ourList.size() - 1)
            selectedSlotIndexIn--;
        super.setSelectedSlotIndex(selectedSlotIndexIn);
    }
}
