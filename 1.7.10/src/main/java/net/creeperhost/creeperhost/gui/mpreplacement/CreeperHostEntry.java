package net.creeperhost.creeperhost.gui.mpreplacement;

import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.api.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMainMenu;
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
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
    private float transparency = 0.5F;

    private int exitX = 0;
    private int exitY = 0;
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
        int i = 1;
        if (isHovering) {
            if (transparency <= 1.0F)
                transparency += 0.04;
            if(isInRect(exitX, exitY, 15, this.mc.fontRenderer.FONT_HEIGHT + 10, mouseX, mouseY )){
                i = 2;
            }
        } else {
            if (transparency >= 0.5F)
                transparency -= 0.04;
            i = 1;
        }

        this.mc.getTextureManager().bindTexture(serverIcon);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, transparency);
        Gui.func_146110_a(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        this.mc.fontRenderer.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + 1, 16777215);
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        this.mc.currentScreen.drawTexturedModalRect(x + listWidth - 12- 2, y-1, 0, 46 + i * 20, 15, this.mc.fontRenderer.FONT_HEIGHT + 10);
        this.mc.fontRenderer.drawString("X", x + listWidth - this.mc.fontRenderer.getStringWidth("X") - 2, y + 1, 16777215);
        exitX = x + listWidth - 12- 2;
        exitY = y- 5;
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
        if(isInRect(exitX, exitY, 15, this.mc.fontRenderer.FONT_HEIGHT + 10, mouseX, mouseY )){
            Config.getInstance().setMpMenuEnabled(false);
            CreeperHost.instance.saveConfig();
            Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
            return true;
        }
        Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
        return true;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }
    public boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY) {
        return ((mouseX >= x && mouseX <= x + xSize) && (mouseY >= y && mouseY <= y + ySize));
    }
}