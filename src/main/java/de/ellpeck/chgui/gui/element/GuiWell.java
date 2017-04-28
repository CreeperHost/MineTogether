package de.ellpeck.chgui.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;

/**
 * Created by Aaron on 28/04/2017.
 */
public class GuiWell extends GuiSlot
{
    public GuiWell(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
    }

    @Override
    protected int getSize()
    {
        return 0;
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
    {

    }

    @Override
    protected boolean isSelected(int slotIndex)
    {
        return false;
    }

    @Override
    protected void drawBackground()
    {

    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn)
    {

    }
}
