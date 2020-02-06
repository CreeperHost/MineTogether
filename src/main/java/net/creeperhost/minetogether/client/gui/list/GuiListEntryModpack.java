package net.creeperhost.minetogether.client.gui.list;

import net.creeperhost.minetogether.client.gui.order.GuiModPackList;
import net.creeperhost.minetogether.data.ModPack;

public class GuiListEntryModpack extends GuiListEntry
{
    private GuiModPackList modpackList;
    ModPack modpack;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;

    public GuiListEntryModpack(GuiModPackList modPackList, GuiList list, ModPack modpack)
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

    public ModPack getModpack()
    {
        return modpack;
    }
}
