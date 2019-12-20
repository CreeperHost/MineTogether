package net.creeperhost.minetogether.client.gui.list;

import net.creeperhost.minetogether.client.gui.GuiModPackList;
import net.creeperhost.minetogether.paul.Callbacks;

public class GuiListEntryModpack extends GuiListEntry
{
    private GuiModPackList modpackList;
    Callbacks.Modpack modpack;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;

    public GuiListEntryModpack(GuiModPackList modPackList, GuiList list, Callbacks.Modpack modpack)
    {
        super(list);
        this.modpackList = modPackList;
        this.modpack = modpack;
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRenderer.getStringWidth(cross);
    }
    
    @Override
    public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        super.render(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, p_render_9_);
    
        if (isSelected)
        {
            if (transparency <= 1.0F)
                transparency += 0.04;
        } else
        {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }
    
        this.mc.fontRenderer.drawString(modpack.getName() + " (" + modpack.getDisplayVersion() + ")", x + 5, y + 5, 16777215);
    }
    
    public Callbacks.Modpack getModpack()
    {
        return modpack;
    }

//    @Override
//    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
//    {
//        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
//    }
}
