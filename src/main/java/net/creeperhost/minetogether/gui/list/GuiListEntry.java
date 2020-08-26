package net.creeperhost.minetogether.gui.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

public class GuiListEntry implements GuiListExtended.IGuiListEntry
{
    protected final Minecraft mc;
    protected final GuiList list;
    
    public GuiListEntry(GuiList list)
    {
        this.list = list;
        this.mc = this.list.gui.mc;
    }
    
    public void setSelected(int par1, int par2, int par3) {}
    
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {}
    
    public void func_192633_a(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
    {
        setSelected(p_192633_1_, p_192633_2_, p_192633_3_);
    }
    
    public void func_192634_a(int p_192634_1_, int p_192634_2_, int p_192634_3_, int p_192634_4_, int p_192634_5_, int p_192634_6_, int p_192634_7_, boolean p_192634_8_, float p_192634_9_)
    {
        drawEntry(p_192634_1_, p_192634_2_, p_192634_3_, p_192634_4_, p_192634_5_, p_192634_6_, p_192634_7_, p_192634_8_);
    }
    
    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        if (this.list.getCurrSelected() != this)
        {
            this.list.setCurrSelected(this);
            return true;
        } else
        {
            return false;
        }
    }
    
    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}
}
