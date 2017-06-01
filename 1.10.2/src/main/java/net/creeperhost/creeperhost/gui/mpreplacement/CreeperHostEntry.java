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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CreeperHostEntry extends ServerListEntryNormal
{
    private final Minecraft mc = Minecraft.getMinecraft();

    private ResourceLocation serverIcon;
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("creeperhost", "textures/hidebtn.png");

    private GuiMultiplayer ourMP;

    private float transparency = 0.5F;

    private int exitX = 0;
    private int exitY = 0;

    protected CreeperHostEntry(GuiMultiplayer p_i45048_1_, ServerData serverIn) {
        super(p_i45048_1_, serverIn);
        ourMP = p_i45048_1_;
        serverIcon = Config.getInstance().isServerHostMenuImage() ? CreeperHost.instance.getImplementation().getMenuIcon() : new ResourceLocation("creeperhost", "textures/nobrandmp.png");
    }

    public CreeperHostEntry(GuiMultiplayer p_i45048_1_, ServerData serverIn, boolean diffSig) {
        this(p_i45048_1_, serverIn);
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        if (isHovering) {
            if (transparency <= 1.0F)
                transparency += 0.04;
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            this.ourMP.drawModalRectWithCustomSizedTexture(x + listWidth - 60- 2, y+4, 0.0F, 0.0F, 60, 10,60F , 10F);
            exitX = x + listWidth - 60- 2;
            exitY = y+4;
        } else {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }

        this.mc.getTextureManager().bindTexture(serverIcon);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, transparency);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        this.mc.fontRendererObj.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + 1, 16777215);

        String s = Util.localize(Config.getInstance().isServerHostMenuImage() ? "mp.clickherebrand" : "mp.clickherebranding");

        this.mc.fontRendererObj.drawString(s, x + 32 + 3, y + this.mc.fontRendererObj.FONT_HEIGHT + 3, 8421504);
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
        if(isInRect(exitX, exitY, 60, 10, mouseX, mouseY )){
            Config.getInstance().setMpMenuEnabled(false);
            CreeperHost.instance.saveConfig();
            this.mc.displayGuiScreen(new GuiMultiplayer(null));
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