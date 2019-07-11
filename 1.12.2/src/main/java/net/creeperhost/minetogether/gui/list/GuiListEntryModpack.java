package net.creeperhost.minetogether.gui.list;

import net.creeperhost.minetogether.gui.GuiModPackList;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class GuiListEntryModpack extends GuiListEntry
{
    GuiModPackList modpackList;
    Callbacks.Modpack modpack;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;
    private boolean wasHovering;
    ResourceLocation resourceLocationCreeperLogo = new ResourceLocation("creeperhost", "textures/icon2.png");

    public GuiListEntryModpack(GuiModPackList modPackList, GuiList list, Callbacks.Modpack modpack)
    {
        super(list);
        this.modpackList = modPackList;
        this.modpack = modpack;
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRendererObj.getStringWidth(cross);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        if (isSelected)
        {
            if (transparency <= 1.0F)
                transparency += 0.04;
        } else
        {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }

        this.mc.fontRendererObj.drawString(modpack.getName(), x + 5, y + 5, 16777215);

        int transparentString = (int) (transparency * 254) << 24;

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        this.mc.fontRendererObj.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationCreeperLogo);

        Gui.drawModalRectWithCustomSizedTexture(listWidth + x - 14,  y + 20, 0.0F, 0.0F, 10, 10, 10F, 10F);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
        }
        else if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 2 + x && mouseY >= y && mouseY <= y + 27) {
            wasHovering = true;
        }
        else if (wasHovering)
        {
            wasHovering = false;
        }
    }

    public Callbacks.Modpack getModpack()
    {
        return modpack;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
    {
        return super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, x, y);
    }
}
