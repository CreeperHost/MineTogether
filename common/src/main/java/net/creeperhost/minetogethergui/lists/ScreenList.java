package net.creeperhost.minetogethergui.lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;

public class ScreenList<T extends ScreenListEntry> extends AbstractSelectionList
{
    public final Screen screen;
    public final int width;
    public final int height;
    public final int rowWidth;
    public int scrollBarPosition = -1;

    public ScreenList(Screen screen, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.screen = screen;
        this.width  = widthIn;
        this.height = heightIn;
        this.rowWidth = 220;
    }

    public ScreenList(Screen screen, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, int rowWidth)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.screen = screen;
        this.width  = widthIn;
        this.height = heightIn;
        this.rowWidth = rowWidth;
    }

    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    public void add(T entry)
    {
        addEntry(entry);
    }
    
    public void clearList()
    {
        clearEntries();
    }
    
    public T getCurrSelected()
    {
        return (T) getSelected();
    }
    
    public int getRowTop(T entry)
    {
        return getRowTop(this.children().indexOf(entry));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput)
    {

    }

    public void setScrollBarPosition(int value)
    {
        this.scrollBarPosition = value;
    }

    @Override
    public int getScrollbarPosition()
    {
        return scrollBarPosition == -1 ? super.getScrollbarPosition() : scrollBarPosition;
    }
}
