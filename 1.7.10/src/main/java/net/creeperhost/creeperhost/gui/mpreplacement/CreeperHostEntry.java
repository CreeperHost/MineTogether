package net.creeperhost.creeperhost.gui.mpreplacement;

import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.api.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
//import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class CreeperHostEntry extends ServerListEntryNormal
{
    private final Minecraft mc = Minecraft.getMinecraft();

    private static ResourceLocation serverIcon;
    private float transparency = 0.5F;

    protected CreeperHostEntry(GuiMultiplayer p_i45048_1_, ServerData serverIn) {
        super(p_i45048_1_, serverIn);
        serverIcon = Config.getInstance().isServerHostMenuImage() ? CreeperHost.instance.getImplementation().getMenuIcon() : new ResourceLocation("creeperhost", "textures/nobrandmp.png");
    }

    public CreeperHostEntry(GuiMultiplayer p_i45048_1_, ServerData serverIn, boolean diffSig) {
        this(p_i45048_1_, serverIn);
    }


    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator p_148279_6, int mouseX, int mouseY, boolean isHovering)
    {
        if (isHovering) {
            if (transparency <= 1.0F)
                transparency += 0.04;
        } else {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }

        this.mc.getTextureManager().bindTexture(serverIcon);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, transparency);
        Gui.func_146110_a(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        this.mc.fontRenderer.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + 1, 16777215);
        String s = Util.localize(Config.getInstance().isServerHostMenuImage() ? "mp.clickherebrand" : "mp.clickherebranding");

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