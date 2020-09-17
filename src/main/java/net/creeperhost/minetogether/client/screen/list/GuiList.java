package net.creeperhost.minetogether.client.screen.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.util.List;

public class GuiList<T extends GuiListEntry> extends ExtendedList
{
    public final Screen gui;
    
    public GuiList(Screen gui, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.gui = gui;
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
        return getRowTop(this.children.indexOf(entry));
    }

}