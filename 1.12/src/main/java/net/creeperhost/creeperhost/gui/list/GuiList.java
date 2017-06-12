package net.creeperhost.creeperhost.gui.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

public class GuiList extends GuiListExtended{

    public final GuiScreen gui;
    private final List<GuiListEntry> options = new ArrayList<GuiListEntry>();
    private int currSelected = -1;

    public GuiList(GuiScreen gui, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn){
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.gui = gui;
    }

    public void addEntry(GuiListEntry entry){
        this.options.add(entry);
    }

    public void setCurrSelected(GuiListEntry entry){
        if(entry != null){
            this.currSelected = this.options.indexOf(entry);
        }
        else{
            this.currSelected = -1;
        }
    }

    public GuiListEntry getCurrSelected(){
        if(this.currSelected >= 0 && this.options.size() > this.currSelected){
            return this.options.get(this.currSelected);
        }
        else{
            this.currSelected = -1;
            return null;
        }
    }

    @Override
    protected boolean isSelected(int slotIndex){
        return slotIndex == this.currSelected;
    }

    @Override
    public IGuiListEntry getListEntry(int index){
        return this.options.get(index);
    }

    @Override
    protected int getSize(){
        return this.options.size();
    }
}