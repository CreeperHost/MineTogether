package net.creeperhost.creeperhost.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by Aaron on 28/04/2017.
 */
public class GuiWell
{
    private final int top;
    private final int bottom;
    private final int right;
    private final int left;
    private final Minecraft mc;
    public List<String> lines;
    private boolean centeredF;
    private String title;

    public GuiWell(Minecraft mcIn, int width, int topIn, int bottomIn, String title, List<String> linesToDraw, boolean centred, int left)
    {
        this.mc = mcIn;
        this.title = title;
        this.lines = linesToDraw;
        this.centeredF = centred;
        this.top = topIn;
        this.bottom = bottomIn;
        this.left = left;
        this.right = width;
    }

    public void drawScreen()
    {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tessellator = Tessellator.instance;


        this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f1 = 32.0F;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(2105376);
        tessellator.addVertexWithUV((double)this.left, (double)this.bottom, 0.0D, (double)((float)this.left / f1), (double)((float)this.bottom / f1));
        tessellator.addVertexWithUV((double)this.right, (double)this.bottom, 0.0D, (double)((float)this.right / f1), (double)((float)this.bottom / f1));
        tessellator.addVertexWithUV((double)this.right, (double)this.top, 0.0D, (double)((float)this.right / f1), (double)((float)this.top / f1));
        tessellator.addVertexWithUV((double)this.left, (double)this.top, 0.0D, (double)((float)this.left / f1), (double)((float)this.top/ f1));
        tessellator.draw();


        GL11.glDisable(GL11.GL_DEPTH_TEST);
        byte b0 = 4;
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 0, 1);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(0, 0);
        tessellator.addVertexWithUV((double)this.left, (double)(this.top + b0), 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double)this.right, (double)(this.top + b0), 0.0D, 1.0D, 1.0D);
        tessellator.setColorRGBA_I(0, 255);
        tessellator.addVertexWithUV((double)this.right, (double)this.top, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double)this.left, (double)this.top, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(0, 255);
        tessellator.addVertexWithUV((double)this.left, (double)this.bottom, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double)this.right, (double)this.bottom, 0.0D, 1.0D, 1.0D);
        tessellator.setColorRGBA_I(0, 0);
        tessellator.addVertexWithUV((double)this.right, (double)(this.bottom - b0), 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double)this.left, (double)(this.bottom - b0), 0.0D, 0.0D, 0.0D);
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title, this.left + ((this.right - this.left) / 2) - (titleWidth / 2), this.top + 2, 0xFFFFFF);

        int topStart = this.top + 15;

        for (String line: lines)
        {
            if (centeredF) {
                int stringWidth = fontRenderer.getStringWidth(line);
                fontRenderer.drawStringWithShadow(line, this.left + ((this.right - this.left) / 2) - (stringWidth / 2), topStart, 0xFFFFFF);
            } else {
                fontRenderer.drawStringWithShadow(line, this.left, topStart, 0xFFFFFF);
            }
            topStart+= 10;
        }

    }
}
