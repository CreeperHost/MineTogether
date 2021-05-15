package net.creeperhost.minetogethergui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ButtonMap extends Button
{
    String buttonText;
    List<Component> tooltiplist = new ArrayList<>();
    int imageWidth;
    int imageHeight;
    float x;
    float y;
    String texturePath;

    public ButtonMap(float x, float y, int width, int height, int imageWidth, int imageHeight, String title, boolean active, String texturePath, Button.OnPress pressedAction)
    {
        super((int)x, (int)y, width, height, new TranslatableComponent(title), pressedAction);
        this.buttonText = title;
        this.x = x;
        this.y = y;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        tooltiplist.add(new TranslatableComponent(buttonText));
        this.active = active;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.color4f(1F, 1F, 1F, alpha);

        Minecraft minecraft = Minecraft.getInstance();

        ResourceLocation map = new ResourceLocation(texturePath + buttonText + ".png");
        minecraft.getTextureManager().bind(map);

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
        drawModalRectWithCustomSizedTextureFloat(poseStack.last().pose(), x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.color4f(1F, 1F, 1F, alpha);
    }

    public static void drawModalRectWithCustomSizedTextureFloat(Matrix4f matrix, float x, float y, float u, float v, int width, int height, float textureWidth, float textureHeight)
    {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX);

        bufferbuilder.vertex(matrix, (float)x, (float)(y + height), 0.0F).uv((float) (u * f), (float)((v + (float)height) * f1)).endVertex();
        bufferbuilder.vertex(matrix, (float)(x + width), (float)(y + height), 0.0F).uv((float)((u + (float)width) * f), (float)((v + (float)height) * f1)).endVertex();
        bufferbuilder.vertex(matrix, (float)(x + width), (float)y, 0.0F).uv((float)((u + (float)width) * f), (float)(v * f1)).endVertex();
        bufferbuilder.vertex(matrix, (float)x, (float)y, 0.0F).uv((float)(u * f), (float)(v * f1)).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        BufferUploader.end(bufferbuilder);
    }
}
