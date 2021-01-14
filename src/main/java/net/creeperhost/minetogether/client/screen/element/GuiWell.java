package net.creeperhost.minetogether.client.screen.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
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
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        
        int titleWidth = fontRenderer.getStringWidth(title);
        MatrixStack matrixStack = new MatrixStack();
        AbstractGui.fill(matrixStack, left, top, right, bottom, 0x66000000);

        fontRenderer.drawStringWithShadow(matrixStack, title, this.left + ((this.right - this.left) / 2) - (titleWidth / 2), this.top + 2, 0xFFFFFF);

        int topStart = this.top + 15;
        
        for (String line : lines)
        {
            if (centeredF)
            {
                int stringWidth = fontRenderer.getStringWidth(line);
                fontRenderer.drawStringWithShadow(matrixStack, line, this.left + ((this.right - this.left) / 2) - (stringWidth / 2), topStart, 0xFFFFFF);
            } else
            {
                fontRenderer.drawStringWithShadow(matrixStack, line, this.left, topStart, 0xFFFFFF);
            }
            topStart += 10;
        }
    }
}
