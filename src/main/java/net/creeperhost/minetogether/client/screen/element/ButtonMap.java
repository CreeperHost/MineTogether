package net.creeperhost.minetogether.client.screen.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.lib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ButtonMap extends Button
{
    String buttonText;
    List<ITextProperties> tooltiplist = new ArrayList<>();
    int imageWidth;
    int imageHeight;
    float x;
    float y;

    public ButtonMap(float x, float y, int width, int height, int imageWidth, int imageHeight, String title, boolean active, IPressable pressedAction)
    {
        super((int)x, (int)y, width, height, new StringTextComponent(title), pressedAction);
        this.buttonText = title;
        this.x = x;
        this.y = y;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        tooltiplist.add(new StringTextComponent(buttonText));
        this.active = active;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.color4f(1F, 1F, 1F, alpha);

        Minecraft minecraft = Minecraft.getInstance();

        ResourceLocation map = new ResourceLocation(Constants.MOD_ID, "textures/map/" + buttonText + ".png");
        minecraft.getTextureManager().bindTexture(map);

        if(isHovered())
        {
            RenderSystem.color4f(0F, 1F, 0F, alpha);
        }
        if(isFocused())
        {
            RenderSystem.color4f(0F, 0.6F, 0F, alpha);
        }
        if(!active)
        {
            RenderSystem.color4f(0.4F, 0.4F, 0.4F, alpha);
        }
        drawModalRectWithCustomSizedTextureFloat(matrixStack.getLast().getMatrix(), x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.color4f(1F, 1F, 1F, alpha);
    }

    public static void drawModalRectWithCustomSizedTextureFloat(Matrix4f matrix, float x, float y, float u, float v, int width, int height, float textureWidth, float textureHeight)
    {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(matrix, (float)x, (float)(y + height), 0.0F).tex((float) (u * f), (float)((v + (float)height) * f1)).endVertex();
        bufferbuilder.pos(matrix, (float)(x + width), (float)(y + height), 0.0F).tex((float)((u + (float)width) * f), (float)((v + (float)height) * f1)).endVertex();
        bufferbuilder.pos(matrix, (float)(x + width), (float)y, 0.0F).tex((float)((u + (float)width) * f), (float)(v * f1)).endVertex();
        bufferbuilder.pos(matrix, (float)x, (float)y, 0.0F).tex((float)(u * f), (float)(v * f1)).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }
}
