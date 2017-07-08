package net.creeperhost.creeperhost.gui.mpreplacement;

import cpw.mods.fml.client.config.GuiUtils;
import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.api.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Iterator;
//import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class CreeperHostEntry extends ServerListEntryNormal
{
    private final Minecraft mc = Minecraft.getMinecraft();

    private ResourceLocation serverIcon;
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("creeperhost", "textures/hidebtn.png");
    protected static final ResourceLocation MPPARTNER_TEXTURES = new ResourceLocation("creeperhost", "textures/mppartner.png");
    private float transparency = 0.5F;

    private int exitX = 0;
    private int exitY = 0;

    private final String cross;
    private final int stringWidth;

    protected CreeperHostEntry(GuiMultiplayer p_i45048_1_, ServerData serverIn) {
        super(p_i45048_1_, serverIn);
        serverIcon = Config.getInstance().isServerHostMenuImage() ? CreeperHost.instance.getImplementation().getMenuIcon() : new ResourceLocation("creeperhost", "textures/nobrandmp.png");
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRenderer.getStringWidth(cross);
    }

    public CreeperHostEntry(GuiMultiplayer p_i45048_1_, ServerData serverIn, boolean diffSig) {
        this(p_i45048_1_, serverIn);
    }

    private int lastWidth;
    private int lastHeight;
    private ScaledResolution res = null;

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator p_148279_6, int mouseX, int mouseY, boolean isHovering) {

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

        mc.getTextureManager().bindTexture(MPPARTNER_TEXTURES);
        Gui.func_146110_a(x + 35, y, 0.0F, 0.0F, 46, 10, 46F, 10F);

        int transparentString = (int) (transparency * 254) << 24;
        this.drawGradientRect(300, listWidth + x - stringWidth - 5, y - 1, listWidth + x - 3, y + 8 + 1, 0x90000000, 0x90000000);
        GL11.glEnable(GL11.GL_BLEND);

        this.mc.fontRenderer.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 1, 16777215 + transparentString);

        String s = Util.localize(Config.getInstance().isServerHostMenuImage() ? "mp.clickherebrand" : "mp.clickherebranding");

        this.mc.fontRenderer.drawString(s, x + 32 + 3, y + (this.mc.fontRenderer.FONT_HEIGHT * 2) + 3, 8421504 + transparentString);

        this.mc.fontRenderer.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);

        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7) {
            if (lastWidth != this.mc.displayWidth || lastHeight != this.mc.displayHeight || res == null) {
                res = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
                lastWidth = this.mc.displayWidth;
                lastHeight = this.mc.displayHeight;
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);


            final int tooltipX = mouseX - 72;
            final int tooltipY = mouseY + ((res.getScaledHeight() / 2 >= mouseY) ? 11 : -11);
            final int tooltipTextWidth = 56;
            final int tooltipHeight = 7;

            final int zLevel = 300;

            // re-purposed code from tooltip rendering
            final int backgroundColor = 0xF0100010;
            this.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
            this.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            this.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            this.drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            this.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            final int borderColorStart = 0x505000FF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            this.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            this.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            this.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
            this.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, transparency);
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            Gui.func_146110_a(mouseX - 74, tooltipY - 1, 0.0F, 0.0F, 60, 10, 60F, 10F);
        }
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }

    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     */
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
    {
        if (x >= 303 - stringWidth - 2 && x <= 303 - 3 && y >= 0 && y <= 7)
        {
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

    // Taken from Gui.class in order to avoid protected access
    private void drawGradientRect(int zLevel, int p_73733_1_, int p_73733_2_, int p_73733_3_, int p_73733_4_, int p_73733_5_, int p_73733_6_)
    {
        float f = (float)(p_73733_5_ >> 24 & 255) / 255.0F;
        float f1 = (float)(p_73733_5_ >> 16 & 255) / 255.0F;
        float f2 = (float)(p_73733_5_ >> 8 & 255) / 255.0F;
        float f3 = (float)(p_73733_5_ & 255) / 255.0F;
        float f4 = (float)(p_73733_6_ >> 24 & 255) / 255.0F;
        float f5 = (float)(p_73733_6_ >> 16 & 255) / 255.0F;
        float f6 = (float)(p_73733_6_ >> 8 & 255) / 255.0F;
        float f7 = (float)(p_73733_6_ & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex((double)p_73733_3_, (double)p_73733_2_, (double)zLevel);
        tessellator.addVertex((double)p_73733_1_, (double)p_73733_2_, (double)zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex((double)p_73733_1_, (double)p_73733_4_, (double)zLevel);
        tessellator.addVertex((double)p_73733_3_, (double)p_73733_4_, (double)zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

}