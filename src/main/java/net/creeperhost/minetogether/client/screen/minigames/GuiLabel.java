package net.creeperhost.minetogether.client.screen.minigames;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class GuiLabel extends Screen
{
    public int x;
    public int y;
    private final List<String> labels;
    public int id;
    private boolean centered;
    public boolean visible = true;
    private boolean labelBgEnabled;
    private final int textColor;
    private int backColor;
    private int ulColor;
    private int brColor;
    private final FontRenderer fontRenderer;
    private int border;

    public GuiLabel(FontRenderer fontRendererObj, int p_i45540_2_, int p_i45540_3_, int p_i45540_4_, int p_i45540_5_, int p_i45540_6_, int p_i45540_7_)
    {
        super(new StringTextComponent(""));
        this.fontRenderer = fontRendererObj;
        this.id = p_i45540_2_;
        this.x = p_i45540_3_;
        this.y = p_i45540_4_;
        this.width = p_i45540_5_;
        this.height = p_i45540_6_;
        this.labels = Lists.<String>newArrayList();
        this.centered = false;
        this.labelBgEnabled = false;
        this.textColor = p_i45540_7_;
        this.backColor = -1;
        this.ulColor = -1;
        this.brColor = -1;
        this.border = 0;
    }

    public void addLine(String p_175202_1_)
    {
        this.labels.add(I18n.format(p_175202_1_));
    }

    public GuiLabel setCentered()
    {
        this.centered = true;
        return this;
    }

    public void drawLabel(MatrixStack matrixStack, Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            RenderSystem.enableBlend();
            this.drawLabelBackground(matrixStack, mc, mouseX, mouseY);
            int i = this.y + this.height / 2 + this.border / 2;
            int j = i - this.labels.size() * 10 / 2;

            for (int k = 0; k < this.labels.size(); ++k)
            {
                if (this.centered)
                {
                    this.drawCenteredString(matrixStack, this.fontRenderer, this.labels.get(k), this.x + this.width / 2, j + k * 10, this.textColor);
                }
                else
                {
                    this.drawString(matrixStack, this.fontRenderer, this.labels.get(k), this.x, j + k * 10, this.textColor);
                }
            }
        }
    }

    protected void drawLabelBackground(MatrixStack matrixStack, Minecraft mcIn, int p_146160_2_, int p_146160_3_)
    {
        if (this.labelBgEnabled)
        {
            int i = this.width + this.border * 2;
            int j = this.height + this.border * 2;
            int k = this.x - this.border;
            int l = this.y - this.border;
            fill(matrixStack, k, l, k + i, l + j, this.backColor);
            this.drawHorizontalLine(matrixStack, k, k + i, l, this.ulColor);
            this.drawHorizontalLine(matrixStack, k, k + i, l + j, this.brColor);
            drawVerticalLine(matrixStack, k, l, l + j, this.ulColor);
            drawVerticalLine(matrixStack, k + i, l, l + j, this.brColor);
        }
    }

    public void drawHorizontalLine(MatrixStack matrixStack, int startX, int endX, int y, int color)
    {
        if (endX < startX)
        {
            int i = startX;
            startX = endX;
            endX = i;
        }

        fill(matrixStack, startX, y, endX + 1, y + 1, color);
    }

    public void drawVerticalLine(MatrixStack matrixStack, int x, int startY, int endY, int color)
    {
        if (endY < startY)
        {
            int i = startY;
            startY = endY;
            endY = i;
        }
        fill(matrixStack, x, startY + 1, x + 1, endY, color);
    }
}
