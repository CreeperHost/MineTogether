package de.ellpeck.chgui.gui.mpreplacement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerSelectionList;

/**
 * Created by Aaron on 26/04/2017.
 */
public class CreeperHostServerSelectionList extends ServerSelectionList
{
    private CreeperHostEntry entry;
    public CreeperHostServerSelectionList(GuiMultiplayer ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(ownerIn, mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        entry = new CreeperHostEntry();
    }

    @Override
    public GuiListExtended.IGuiListEntry getListEntry(int index) {
        if (index == getSize() - 1) {
            return entry;
        }
        return super.getListEntry(index);
    }

    @Override
    public int getSize() {
        return super.getSize() + 1;
    }
}
