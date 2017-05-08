package de.ellpeck.creeperhost.gui.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

public class GuiListEntry implements GuiListExtended.IGuiListEntry{

    protected final Minecraft mc;
    protected final GuiList list;

    public GuiListEntry(GuiList list){
        this.list = list;
        this.mc = this.list.gui.mc;
    }

    @Override
    public void setSelected(int par1, int par2, int par3){

    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected){
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY){
        if(this.list.getCurrSelected() != this){
            this.list.setCurrSelected(this);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY){

    }
}
