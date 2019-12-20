package net.creeperhost.minetogether.client.gui.mpreplacement;

import com.mojang.blaze3d.platform.GlStateManager;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.client.gui.GuiGetServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class CreeperHostEntry extends ServerData
{
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("creeperhost", "textures/hidebtn.png");
    protected static final ResourceLocation MPPARTNER_TEXTURES = new ResourceLocation("creeperhost", "textures/mppartner.png");
    private final Minecraft mc = Minecraft.getInstance();
    private final String cross;
    private final int stringWidth;
    private ResourceLocation serverIcon;
    private MultiplayerScreen ourMP;
    
    private float transparency = 0.5F;
    private int lastWidth;
    private int lastHeight;
//    private ScaledResolution res = null;
    
    protected CreeperHostEntry(String name, String ip, boolean isLan)
    {
        super(name, ip, isLan);
//        ourMP = p_i45048_1_;
        serverIcon = Config.getInstance().isServerHostMenuImage() ? MineTogether.instance.getImplementation().getMenuIcon() : new ResourceLocation("creeperhost", "textures/nobrandmp.png");
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRenderer.getStringWidth(cross);
    }
    
//    public CreeperHostEntry(MultiplayerScreen p_i45048_1_, ServerData serverIn, boolean diffSig)
//    {
//        this(p_i45048_1_, serverIn);
//    }
    
    public void func_192634_a(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float newthingy)
    {
        ourDrawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering);
    }
    
    // < 1.12 compat
    public void func_180790_a(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        ourDrawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering);
    }
    
    @SuppressWarnings("Duplicates")
    public void ourDrawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        if (isHovering)
        {
            if (transparency <= 1.0F)
                transparency += 0.04;
        } else
        {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }
        
        this.mc.getTextureManager().bindTexture(serverIcon);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, transparency);
//        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        this.mc.fontRenderer.drawString(Util.localize("mp.partner"), x+35, y, 16777215);
        int transparentString = (int) (transparency * 254) << 24;
        GuiUtils.drawGradientRect(300, listWidth + x - stringWidth - 5, y - 1, listWidth + x - 3, y + 8 + 1, 0x90000000, 0x90000000);
        GlStateManager.enableBlend();
        this.mc.fontRenderer.drawString(Util.localize("mp.getserver"), x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 1, 16777215 + transparentString);
        String s = Util.localize(Config.getInstance().isServerHostMenuImage() ? "mp.clickherebrand" : "mp.clickherebranding");
        this.mc.fontRenderer.drawString(s, x + 32 + 3, y + (this.mc.fontRenderer.FONT_HEIGHT * 2) + 3, 8421504 + transparentString);
        this.mc.fontRenderer.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
//            if (lastWidth != this.mc.displayWidth || lastHeight != this.mc.displayHeight || res == null)
//            {
//                res = new ScaledResolution(this.mc);
//                lastWidth = this.mc.displayWidth;
//                lastHeight = this.mc.displayHeight;
//            }
//
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            
            final int tooltipX = mouseX - 72;
            final int tooltipY = mouseY + 11;
            final int tooltipTextWidth = 56;
            final int tooltipHeight = 7;
            
            final int zLevel = 300;
            
            // re-purposed code from tooltip rendering
            final int backgroundColor = 0xF0100010;
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            final int borderColorStart = 0x505000FF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
            
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, transparency);
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
//            Gui.drawModalRectWithCustomSizedTexture(mouseX - 74, tooltipY - 1, 0.0F, 0.0F, 60, 10, 60F, 10F);
        }
    }
    
    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }
    
    @SuppressWarnings("Duplicates")
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
    {
        if (x >= 303 - stringWidth - 2 && x <= 303 - 3 && y >= 0 && y <= 7)
        {
            Config.getInstance().setMpMenuEnabled(false);
            MineTogether.instance.saveConfig();
            this.mc.displayGuiScreen(new MultiplayerScreen(null));
            return true;
        }
        Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
        return true;
    }
    
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }
}