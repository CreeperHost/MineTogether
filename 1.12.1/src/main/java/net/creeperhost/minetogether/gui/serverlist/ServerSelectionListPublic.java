package net.creeperhost.minetogether.gui.serverlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;

import java.util.List;

/**
 * Created by Aaron on 19/05/2017.
 */
public class ServerSelectionListPublic extends ServerSelectionList
{

    private List<ServerListEntryNormal> ourList;
    private GuiMultiplayer ourParent;

    public ServerSelectionListPublic(GuiMultiplayer ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(ownerIn, mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        ourParent = ownerIn;
    }

    @Override
    protected int getSize()
    {
        return super.getSize() - 1;
    }


    @Override
    public GuiListExtended.IGuiListEntry getListEntry(int index)
    {
        return super.getListEntry(index);
    }
}
