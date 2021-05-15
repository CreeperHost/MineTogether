package net.creeperhost.minetogethergui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

/**
 * Created by Aaron on 28/04/2017.
 */
public class ScreenWell
{
    private final Minecraft mc;
    public final int top;
    public final int bottom;
    public final int right;
    public final int left;
    public List<String> lines;
    private boolean centeredF;
    private String title;

    public ScreenWell(Minecraft mcIn, int width, int topIn, int bottomIn, String title, List<String> linesToDraw, boolean centred, int left)
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
    public void render()
    {
        Font fontRenderer = Minecraft.getInstance().font;
        
        int titleWidth = fontRenderer.width(title);
        PoseStack matrixStack = new PoseStack();
        Screen.fill(matrixStack, left, top, right, bottom, 0x66000000);

        fontRenderer.drawShadow(matrixStack, title, this.left + ((this.right - this.left) / 2) - (titleWidth / 2), this.top + 2, 0xFFFFFF);

        int topStart = this.top + 15;
        
        for (String line : lines)
        {
            if (centeredF)
            {
                int stringWidth = fontRenderer.width(line);
                fontRenderer.drawShadow(matrixStack, line, this.left + ((this.right - this.left) / 2) - (stringWidth / 2), topStart, 0xFFFFFF);
            } else
            {
                fontRenderer.drawShadow(matrixStack, line, this.left, topStart, 0xFFFFFF);
            }
            topStart += 10;
        }
    }
}
