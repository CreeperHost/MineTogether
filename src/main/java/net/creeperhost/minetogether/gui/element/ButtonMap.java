package net.creeperhost.minetogether.gui.element;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ButtonMap extends FancyButton
{
    String buttonText;
    boolean focus;
    int imageWidth;
    int imageHeight;
    double x;
    double y;

    public ButtonMap(int id, double xPos, double yPos, int width, int height, int imageWidth, int imageHeight, String displayString, boolean active, FancyButton.IPressable pressedAction)
    {
        super(id, (int)xPos, (int)yPos, width, height, displayString, pressedAction);
        this.x = xPos;
        this.y = yPos;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.buttonText = displayString;
        this.enabled = active;
        this.focus = false;
    }

    public boolean isFocus()
    {
        return focus;
    }

    public void setFocus(boolean focus)
    {
        this.focus = focus;
    }

    @Override
    public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float partial)
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        ResourceLocation map = new ResourceLocation(CreeperHost.MOD_ID, "textures/map/" + buttonText + ".png");
        minecraft.getTextureManager().bindTexture(map);

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        int k = this.getHoverState(this.hovered);

        if(this.hovered)
        {
            GlStateManager.color(0F, 1F, 0F, 1.0F);
        }
        if(isFocus())
        {
            GlStateManager.color(0F, 0.6F, 0F, 1.0F);
        }
        if(!enabled)
        {
            GlStateManager.color(0.4F, 0.4F, 0.4F, 1.0F);
        }
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        GlStateManager.color(1F, 1F, 1F, 1.0F);
    }

    public static void drawModalRectWithCustomSizedTexture(double x, double y, float u, float v, int width, int height, float textureWidth, float textureHeight)
    {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)x, (double)(y + height), 0.0D).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + height), 0.0D).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)y, 0.0D).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
        bufferbuilder.pos((double)x, (double)y, 0.0D).tex((double)(u * f), (double)(v * f1)).endVertex();
        tessellator.draw();
    }
}
