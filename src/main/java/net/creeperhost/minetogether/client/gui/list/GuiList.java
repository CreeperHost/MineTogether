package net.creeperhost.minetogether.client.gui.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.util.ArrayList;
import java.util.List;

public class GuiList<T extends GuiListEntry> extends ExtendedList
{
    public final Screen gui;
    private List<T> options;
    private int currSelected = -1;
    
    public GuiList(Screen gui, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.gui = gui;
        options = new ArrayList<T>();
    }
    
    public void addEntry(T entry)
    {
        this.options.add(entry);
    }
    
    public T getCurrSelected()
    {
        if (this.currSelected >= 0 && this.options.size() > this.currSelected)
        {
            return this.options.get(this.currSelected);
        } else
        {
            this.currSelected = -1;
            return null;
        }
    }
    
    public void setCurrSelected(T entry)
    {
        if (entry != null)
        {
            this.currSelected = this.options.indexOf(entry);
        } else
        {
            this.currSelected = -1;
        }
    }
    
    public void clearList()
    {
        options = new ArrayList<T>();
    }

    @Override
    protected boolean isSelectedItem(int slotIndex)
    {
        return super.isSelectedItem(slotIndex);
    }
}