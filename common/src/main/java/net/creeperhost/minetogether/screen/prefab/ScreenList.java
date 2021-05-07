package net.creeperhost.minetogether.screen.prefab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;

public class ScreenList<T extends ScreenListEntry> extends AbstractSelectionList
{
    public final Screen screen;

    public ScreenList(Screen screen, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.screen = screen;
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

}