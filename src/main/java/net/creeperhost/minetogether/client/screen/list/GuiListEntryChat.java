package net.creeperhost.minetogether.client.gui.list;

import net.minecraft.util.text.ITextComponent;

public class GuiListEntryChat extends GuiListEntry
{
    ITextComponent component;
    
    public GuiListEntryChat(GuiList list, ITextComponent component)
    {
        super(list);
        this.component = component;
    }
    
    @Override
    public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        super.render(slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, p_render_9_);
        this.mc.fontRenderer.drawString(component.getFormattedText(), x + 5, y + 5, 16777215);
    }
}
