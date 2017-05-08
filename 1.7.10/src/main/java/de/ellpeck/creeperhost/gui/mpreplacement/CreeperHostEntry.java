package de.ellpeck.creeperhost.gui.mpreplacement;

import de.ellpeck.creeperhost.Util;
import de.ellpeck.creeperhost.gui.GuiGetServer;
import de.ellpeck.creeperhost.paul.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class CreeperHostEntry implements GuiListExtended.IGuiListEntry
{
    private final Minecraft mc = Minecraft.getMinecraft();

    private static ResourceLocation serverIcon;

    public CreeperHostEntry() {
        serverIcon = new ResourceLocation("creeperhost", "textures/creeperhost.png");
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator p_148279_6, int mouseX, int mouseY, boolean isSelected)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(serverIcon);
        Gui.func_146110_a(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        this.mc.fontRenderer.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + 1, 16777215);
        String s = Util.localize("mp.clickhere");

        this.mc.fontRenderer.drawString(s, x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 3, 8421504);
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }

    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     */
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
        return true;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }
}