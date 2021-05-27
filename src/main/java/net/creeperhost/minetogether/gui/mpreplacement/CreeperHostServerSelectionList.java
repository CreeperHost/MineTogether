package net.creeperhost.minetogether.gui.mpreplacement;

import net.creeperhost.minetogether.mtconnect.ConnectHelper;
import net.creeperhost.minetogether.mtconnect.LanServerInfoConnect;
import net.creeperhost.minetogether.mtconnect.OurServerListEntryLanDetected;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryLanDetected;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerInfo;

import java.util.*;

/**
 * Created by Aaron on 19/05/2017.
 */
public class CreeperHostServerSelectionList extends ServerSelectionList
{
    private List<ServerListEntryNormal> ourOnlineList;
    private List<ServerListEntryLanDetected> ourNetworkList;
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
        ourOnlineList.add(new CreeperHostEntry(ourParent, new ServerData("", "127.0.0.1", false), true));
    }

    @Override
    public void updateNetworkServers(List<LanServerInfo> p_148194_1_) {
        super.updateNetworkServers(p_148194_1_);
        if(ConnectHelper.isEnabled) {
            ArrayList<LanServerInfoConnect> ourServerInfos = new ArrayList<>();
            for (LanServerInfo info: p_148194_1_) {
                if (info instanceof LanServerInfoConnect) {
                    ourServerInfos.add((LanServerInfoConnect) info);
                }
            }

            ourNetworkList.removeIf(entry -> ourServerInfos.contains(entry.getServerData()));
            ourServerInfos.forEach((info) -> ourNetworkList.add(0, new OurServerListEntryLanDetected(ourParent, info)));
        }
    }

    public void replaceOnlineList(List<ServerListEntryNormal> list)
    {
        if (ourOnlineList == list)
            return;
        ourOnlineList = list;
        ourOnlineList.add(new CreeperHostEntry(ourParent, new ServerData("", "127.0.0.1", false), true));
    }

    public void replaceNetworkList(List<ServerListEntryLanDetected> list)
    {
        if (ourNetworkList == list)
            return;
        ourNetworkList = list;
    }

    @Override
    public void setSelectedSlotIndex(int selectedSlotIndexIn) {
        if (selectedSlotIndexIn == ourOnlineList.size() - 1)
            selectedSlotIndexIn--;
        super.setSelectedSlotIndex(selectedSlotIndexIn);
    }
}
