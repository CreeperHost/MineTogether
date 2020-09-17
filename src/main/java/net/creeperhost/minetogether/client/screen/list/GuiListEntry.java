package net.creeperhost.minetogether.client.screen.list;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

public class GuiListEntry extends ExtendedList.AbstractListEntry
{
    protected final Minecraft mc;
    protected final GuiList list;
    
    public GuiListEntry(GuiList list)
    {
        this.list = list;
        this.mc = this.list.gui.getMinecraft();
    }
    
    @Override
    public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
    }
    
    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        list.setSelected(this);
        return false;
    }
}
