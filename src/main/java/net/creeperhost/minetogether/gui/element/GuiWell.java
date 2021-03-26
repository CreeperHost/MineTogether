package net.creeperhost.minetogether.gui.element;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.gui.hacky.IBufferProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.List;

/**
 * Created by Aaron on 28/04/2017.
 */
public class GuiWell
{
    private final Minecraft mc;
    public final int top;
    public final int bottom;
    public final int right;
    public final int left;
    public List<String> lines;
    private boolean centeredF;
    private String title;
    private double zlevel;
    
    public GuiWell(Minecraft mcIn, int width, int topIn, int bottomIn, String title, List<String> linesToDraw, boolean centred, int left, double zLevel)
    {
        this.title = title;
        this.lines = linesToDraw;
        this.centeredF = centred;
        this.mc = mcIn;
        this.top = topIn;
        this.bottom = bottomIn;
        this.left = left;
        this.right = width;
        this.zlevel = zLevel;
    }
    
    @SuppressWarnings("Duplicates")
    public void drawScreen()
    {
//        GlStateManager.disableLighting();
//        GlStateManager.disableFog();
//        Tessellator tessellator = Tessellator.getInstance();
//
//        IBufferProxy buffer = Util.getBufferProxy();
//        this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        float f = 32.0F;
//        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        buffer.pos((double) this.left, (double) this.bottom, 0.0D).tex((double) ((float) this.left / f), (double) ((float) this.bottom / f)).color(32, 32, 32, 255).endVertex();
//        buffer.pos((double) this.right, (double) this.bottom, 0.0D).tex((double) ((float) this.right / f), (double) ((float) this.bottom / f)).color(32, 32, 32, 255).endVertex();
//        buffer.pos((double) this.right, (double) this.top, 0.0D).tex((double) ((float) this.right / f), (double) ((float) this.top / f)).color(32, 32, 32, 255).endVertex();
//        buffer.pos((double) this.left, (double) this.top, 0.0D).tex((double) ((float) this.left / f), (double) ((float) this.top / f)).color(32, 32, 32, 255).endVertex();
//        tessellator.draw();
//
//        GlStateManager.disableDepth();
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
//        GlStateManager.disableAlpha();
//        GlStateManager.shadeModel(7425);
//        GlStateManager.disableTexture2D();
//        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        buffer.pos((double) this.left, (double) (this.top + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
//        buffer.pos((double) this.right, (double) (this.top + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
//        buffer.pos((double) this.right, (double) this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//        buffer.pos((double) this.left, (double) this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//        tessellator.draw();
//        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        buffer.pos((double) this.left, (double) this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
//        buffer.pos((double) this.right, (double) this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
//        buffer.pos((double) this.right, (double) (this.bottom - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
//        buffer.pos((double) this.left, (double) (this.bottom - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
//        tessellator.draw();
//
//        GlStateManager.enableTexture2D();
//        GlStateManager.shadeModel(7424);
//        GlStateManager.enableAlpha();
//        GlStateManager.disableBlend();
        
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        drawGradientRect(left, top, right, bottom, 0x66000000, 0x66000000, zlevel);

        
        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title, this.left + ((this.right - this.left) / 2) - (titleWidth / 2), this.top + 2, 0xFFFFFF);
        
        int topStart = this.top + 15;
        
        for (String line : lines)
        {
            if (centeredF)
            {
                int stringWidth = fontRenderer.getStringWidth(line);
                fontRenderer.drawStringWithShadow(line, this.left + ((this.right - this.left) / 2) - (stringWidth / 2), topStart, 0xFFFFFF);
            } else
            {
                fontRenderer.drawStringWithShadow(line, this.left, topStart, 0xFFFFFF);
            }
            topStart += 10;
        }
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, double zLevel)
    {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)right, (double)top, zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double)left, (double)top, zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double)left, (double)bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
