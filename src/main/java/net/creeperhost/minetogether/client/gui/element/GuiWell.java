package net.creeperhost.minetogether.client.gui.element;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.client.gui.hacky.IBufferProxy;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.List;

/**
 * Created by Aaron on 28/04/2017.
 */
public class GuiWell
{
    private final Minecraft mc;
    private final int top;
    private final int bottom;
    private final int right;
    private final int left;
    public List<String> lines;
    private boolean centeredF;
    private String title;
    
    public GuiWell(Minecraft mcIn, int width, int topIn, int bottomIn, String title, List<String> linesToDraw, boolean centred, int left)
    {
        this.title = title;
        this.lines = linesToDraw;
        this.centeredF = centred;
        this.mc = mcIn;
        this.top = topIn;
        this.bottom = bottomIn;
        this.left = left;
        this.right = width;
    }
    
    @SuppressWarnings("Duplicates")
    public void drawScreen()
    {
        RenderSystem.disableLighting();
        RenderSystem.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        
        IBufferProxy buffer = Util.getBufferProxy();
        this.mc.getTextureManager().bindTexture(Screen.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double) this.left, (double) this.bottom, 0.0D).tex((double) ((float) this.left / f), (double) ((float) this.bottom / f)).color(32, 32, 32, 255).endVertex();
        buffer.pos((double) this.right, (double) this.bottom, 0.0D).tex((double) ((float) this.right / f), (double) ((float) this.bottom / f)).color(32, 32, 32, 255).endVertex();
        buffer.pos((double) this.right, (double) this.top, 0.0D).tex((double) ((float) this.right / f), (double) ((float) this.top / f)).color(32, 32, 32, 255).endVertex();
        buffer.pos((double) this.left, (double) this.top, 0.0D).tex((double) ((float) this.left / f), (double) ((float) this.top / f)).color(32, 32, 32, 255).endVertex();
        tessellator.draw();
        
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double) this.left, (double) (this.top + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos((double) this.right, (double) (this.top + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos((double) this.right, (double) this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double) this.left, (double) this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double) this.left, (double) this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double) this.right, (double) this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double) this.right, (double) (this.bottom - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos((double) this.left, (double) (this.bottom - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        
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
}
