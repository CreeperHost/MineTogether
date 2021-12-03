package net.creeperhost.minetogethergui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.creeperhost.minetogether.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ScreenHelpers
{
    public static AbstractWidget findButton(String buttonString, Screen screen)
    {
        if(screen.children() != null && !screen.children().isEmpty())
        {
            for (GuiEventListener listener : screen.children())
            {
                if(!(listener instanceof Button)) return null;

                Button widget = (Button) listener;

                if (widget.getMessage().getString().equalsIgnoreCase(I18n.get(buttonString)))
                {
                    return widget;
                }
            }
        }
        return null;
    }

    public static AbstractWidget removeButton(String buttonString, Screen screen)
    {
        AbstractWidget widget = findButton(buttonString, screen);
        if(widget != null)
        {
            widget.visible = false;
            widget.active = false;
            return widget;
        }
        return null;
    }

    public static void drawLogo(PoseStack matrixStack, Font fontRendererObj, int containerWidth, int containerHeight, int containerX, int containerY, float scale)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F); // reset alpha
        float adjust = (1 / scale);
        int width = (int) (containerWidth * adjust);
        int height = (int) (containerHeight * adjust);
        int x = (int) (containerX * adjust);
        int y = (int) (containerY * adjust);
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Constants.MOD_ID, "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(Constants.MOD_ID, "textures/minetogether25.png");

//        RenderSystem.pushMatrix();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, scale);
//        RenderSystem.scaled(scale, scale, scale);

        int mtHeight = (int) (318 / 2.5);
        int mtWidth = (int) (348 / 2.5);

        int creeperHeight = 22;
        int creeperWidth = 80;

        int totalHeight = mtHeight + creeperHeight;
        int totalWidth = mtWidth + creeperWidth;

        totalHeight *= adjust;
        totalWidth *= adjust;

        RenderSystem.setShaderTexture(0, resourceLocationMineTogetherLogo);
        RenderSystem.enableBlend();
        GuiComponent.blit(matrixStack, x + (width / 2 - (mtWidth / 2)), y + (height / 2 - (totalHeight / 2)), 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        String created = "Created by";
        int stringWidth = fontRendererObj.width(created);

        int creeperTotalWidth = creeperWidth + stringWidth;
        fontRendererObj.drawShadow(matrixStack, created, x + (width / 2 - (creeperTotalWidth / 2)), y + (height / 2 - (totalHeight / 2) + mtHeight + 7), 0x40FFFFFF);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F); // reset alpha as font renderer isn't nice like that

        RenderSystem.setShaderTexture(0, resourceLocationCreeperLogo);
        RenderSystem.enableBlend();
        GuiComponent.blit(matrixStack, x + (width / 2 - (creeperTotalWidth / 2) + stringWidth), y + (height / 2 - (totalHeight / 2) + mtHeight), 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);

        RenderSystem.disableBlend();
        matrixStack.popPose();
//        RenderSystem.popMatrix();
    }

    public void renderHead(PoseStack poseStack, int x, int y)
    {
        Minecraft.getInstance().getTextureManager().bindForSetup(new ResourceLocation("textures/entity/steve.png"));

        GuiComponent.blit(poseStack, x, y - 2, 9, 9, 8.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, x, y - 2, 9, 9, 40.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.disableBlend();
    }

    public static void loadingSpin(PoseStack poseStack, float partialTicks, int ticks, int x, int y, ItemStack stack)
    {
        int rotateTickMax = 30;
        int throbTickMax = 20;
        int rotateTicks = ticks % rotateTickMax;
        int throbTicks = ticks % throbTickMax;
        poseStack.pushPose();
//        RenderSystem.pushMatrix();
        poseStack.translate(x, y, 0);
//        RenderSystem.translated(x, y, 0);
        float scale = 1F + ((throbTicks >= (throbTickMax / 2) ? (throbTickMax - (throbTicks + partialTicks)) : (throbTicks + partialTicks)) * (2F / throbTickMax));
        poseStack.scale(scale, scale, scale);
//        RenderSystem.scalef(scale, scale, scale);
//        RenderSystem.rotatef((rotateTicks + partialTicks) * (360F / rotateTickMax), 0, 0, 1);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderGuiItem(stack, -8, -8);

        poseStack.popPose();
//        RenderSystem.popMatrix();
    }

    public static void drawModalRectWithCustomSizedTextureFloat(Matrix4f matrix, float x, float y, float u, float v, int width, int height, float textureWidth, float textureHeight)
    {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferbuilder.vertex(matrix, (float)x, (float)(y + height), 0.0F).uv((float) (u * f), (float)((v + (float)height) * f1)).endVertex();
        bufferbuilder.vertex(matrix, (float)(x + width), (float)(y + height), 0.0F).uv((float)((u + (float)width) * f), (float)((v + (float)height) * f1)).endVertex();
        bufferbuilder.vertex(matrix, (float)(x + width), (float)y, 0.0F).uv((float)((u + (float)width) * f), (float)(v * f1)).endVertex();
        bufferbuilder.vertex(matrix, (float)x, (float)y, 0.0F).uv((float)(u * f), (float)(v * f1)).endVertex();
        bufferbuilder.end();
//        RenderSystem.enableAlphaTest();
        BufferUploader.end(bufferbuilder);
    }

    public static void drawContinuousTexturedBox(PoseStack matrixStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;

        // Draw Border
        // Top Left
        drawTexturedModalRect(matrixStack, x, y, u, v, leftBorder, topBorder, zLevel);
        // Top Right
        drawTexturedModalRect(matrixStack, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
        // Bottom Left
        drawTexturedModalRect(matrixStack, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
        // Bottom Right
        drawTexturedModalRect(matrixStack, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);

        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++)
        {
            // Top Border
            drawTexturedModalRect(matrixStack, x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel);
            // Bottom Border
            drawTexturedModalRect(matrixStack, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel);

            // Throw in some filler for good measure
            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
                drawTexturedModalRect(matrixStack, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel);
        }

        // Side Borders
        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
        {
            // Left Border
            drawTexturedModalRect(matrixStack, x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
            // Right Border
            drawTexturedModalRect(matrixStack, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
        }
    }

    public static void drawTexturedModalRect(PoseStack matrixStack, int x, int y, int u, int v, int width, int height, float zLevel)
    {
        final float uScale = 1f / 0x100;
        final float vScale = 1f / 0x100;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder wr = tessellator.getBuilder();
        wr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = matrixStack.last().pose();
        wr.vertex(matrix, x, y + height, zLevel).uv( u * uScale, ((v + height) * vScale)).endVertex();
        wr.vertex(matrix,x + width,y + height, zLevel).uv((u + width) * uScale, ((v + height) * vScale)).endVertex();
        wr.vertex(matrix,x + width, y, zLevel).uv((u + width) * uScale, (v * vScale)).endVertex();
        wr.vertex(matrix, x, y, zLevel).uv( u * uScale, (v * vScale)).endVertex();
        tessellator.end();
    }

    public static void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight)
    {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex((double) x, (double) (y + height), 0.0D).uv((u * f), ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.vertex((double) (x + width), (double) (y + height), 0.0D).uv(((u + (float) uWidth) * f), ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.vertex((double) (x + width), (double) y, 0.0D).uv(((u + (float) uWidth) * f), (v * f1)).endVertex();
        bufferbuilder.vertex((double) x, (double) y, 0.0D).uv((u * f), (v * f1)).endVertex();
        tessellator.end();
    }
}
