package net.creeperhost.minetogethergui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.creeperhost.minetogether.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ScreenHelpers
{
    public static AbstractWidget findButton(String buttonString, List<AbstractWidget> widgetList)
    {
        if(widgetList != null && !widgetList.isEmpty())
        {
            for (AbstractWidget widget : widgetList)
            {
                if (widget.getMessage().getString().equalsIgnoreCase(I18n.get(buttonString)))
                {
                    return widget;
                }
            }
        }
        return null;
    }

    public static AbstractWidget removeButton(String buttonString, List<AbstractWidget> widgetList)
    {
        AbstractWidget widget = findButton(buttonString, widgetList);
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
        RenderSystem.color4f(1F, 1F, 1F, 1F); // reset alpha
        float adjust = (1 / scale);
        int width = (int) (containerWidth * adjust);
        int height = (int) (containerHeight * adjust);
        int x = (int) (containerX * adjust);
        int y = (int) (containerY * adjust);
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Constants.MOD_ID, "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(Constants.MOD_ID, "textures/minetogether25.png");

        RenderSystem.pushMatrix();
        RenderSystem.scaled(scale, scale, scale);

        int mtHeight = (int) (318 / 2.5);
        int mtWidth = (int) (348 / 2.5);

        int creeperHeight = 22;
        int creeperWidth = 80;

        int totalHeight = mtHeight + creeperHeight;
        int totalWidth = mtWidth + creeperWidth;

        totalHeight *= adjust;
        totalWidth *= adjust;

        Minecraft.getInstance().getTextureManager().bind(resourceLocationMineTogetherLogo);
        RenderSystem.enableBlend();
        GuiComponent.blit(matrixStack, x + (width / 2 - (mtWidth / 2)), y + (height / 2 - (totalHeight / 2)), 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        String created = "Created by";
        int stringWidth = fontRendererObj.width(created);

        int creeperTotalWidth = creeperWidth + stringWidth;
        fontRendererObj.drawShadow(matrixStack, created, x + (width / 2 - (creeperTotalWidth / 2)), y + (height / 2 - (totalHeight / 2) + mtHeight + 7), 0x40FFFFFF);
        RenderSystem.color4f(1F, 1F, 1F, 1F); // reset alpha as font renderer isn't nice like that

        Minecraft.getInstance().getTextureManager().bind(resourceLocationCreeperLogo);
        RenderSystem.enableBlend();
        GuiComponent.blit(matrixStack, x + (width / 2 - (creeperTotalWidth / 2) + stringWidth), y + (height / 2 - (totalHeight / 2) + mtHeight), 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);

        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    public static void loadingSpin(float partialTicks, int ticks, int x, int y, ItemStack stack)
    {
        int rotateTickMax = 30;
        int throbTickMax = 20;
        int rotateTicks = ticks % rotateTickMax;
        int throbTicks = ticks % throbTickMax;
        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, 0);
        float scale = 1F + ((throbTicks >= (throbTickMax / 2) ? (throbTickMax - (throbTicks + partialTicks)) : (throbTicks + partialTicks)) * (2F / throbTickMax));
        RenderSystem.scalef(scale, scale, scale);
        RenderSystem.rotatef((rotateTicks + partialTicks) * (360F / rotateTickMax), 0, 0, 1);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderGuiItem(stack, -8, -8);

        RenderSystem.popMatrix();
    }

    public static void renderTooltip(PoseStack poseStack, Font font, Component component, int i, int j, int screenWidth, int screenHeight)
    {
        renderTooltip(poseStack, font, Arrays.asList(component.getVisualOrderText()), screenHeight / 2, screenHeight / 2, screenWidth, screenHeight);
    }

    //Copy pasta from Vanilla
    public static void renderTooltip(PoseStack poseStack, Font font, List<? extends FormattedCharSequence> list, int i, int j, int screenWidth, int screenHeight)
    {
        if (!list.isEmpty())
        {
            int k = 0;
            Iterator var6 = list.iterator();

            while(var6.hasNext())
            {
                FormattedCharSequence formattedCharSequence = (FormattedCharSequence)var6.next();
                int l = font.width(formattedCharSequence);
                if (l > k) {
                    k = l;
                }
            }

            int m = i + 12;
            int n = j - 12;
            int p = 8;
            if (list.size() > 1)
            {
                p += 2 + (list.size() - 1) * 10;
            }
            if (m + k > screenWidth)
            {
                m -= 28 + k;
            }
            if (n + p + 6 > screenHeight)
            {
                n = screenHeight - p - 6;
            }

            poseStack.pushPose();
            int q = -267386864;
            int r = 1347420415;
            int s = 1344798847;
//            int t = true;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix4f = poseStack.last().pose();
            fillGradient(matrix4f, bufferBuilder, m - 3, n - 4, m + k + 3, n - 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, m - 3, n + p + 3, m + k + 3, n + p + 4, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, m - 3, n - 3, m + k + 3, n + p + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, m - 4, n - 3, m - 3, n + p + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, m + k + 3, n - 3, m + k + 4, n + p + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, m - 3, n - 3 + 1, m - 3 + 1, n + p + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferBuilder, m + k + 2, n - 3 + 1, m + k + 3, n + p + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferBuilder, m - 3, n - 3, m + k + 3, n - 3 + 1, 400, 1347420415, 1347420415);
            fillGradient(matrix4f, bufferBuilder, m - 3, n + p + 2, m + k + 3, n + p + 3, 400, 1344798847, 1344798847);
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            poseStack.translate(0.0D, 0.0D, 400.0D);

            for(int u = 0; u < list.size(); ++u) {
                FormattedCharSequence formattedCharSequence2 = (FormattedCharSequence)list.get(u);
                if (formattedCharSequence2 != null) {
                    font.drawInBatch(formattedCharSequence2, (float)m, (float)n, -1, true, matrix4f, bufferSource, false, 0, 15728880);
                }

                if (u == 0) {
                    n += 2;
                }

                n += 10;
            }

            bufferSource.endBatch();
            poseStack.popPose();
        }
    }

    protected static void fillGradient(Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o)
    {
        float f = (float)(n >> 24 & 255) / 255.0F;
        float g = (float)(n >> 16 & 255) / 255.0F;
        float h = (float)(n >> 8 & 255) / 255.0F;
        float p = (float)(n & 255) / 255.0F;
        float q = (float)(o >> 24 & 255) / 255.0F;
        float r = (float)(o >> 16 & 255) / 255.0F;
        float s = (float)(o >> 8 & 255) / 255.0F;
        float t = (float)(o & 255) / 255.0F;
        bufferBuilder.vertex(matrix4f, (float)k, (float)j, (float)m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)i, (float)j, (float)m).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)i, (float)l, (float)m).color(r, s, t, q).endVertex();
        bufferBuilder.vertex(matrix4f, (float)k, (float)l, (float)m).color(r, s, t, q).endVertex();
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

    public static void drawContinuousTexturedBox(PoseStack matrixStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int borderSize, float zLevel)
    {
        drawContinuousTexturedBox(matrixStack, x, y, u, v, width, height, textureWidth, textureHeight, borderSize, borderSize, borderSize, borderSize, zLevel);
    }

    public static void drawContinuousTexturedBox(PoseStack matrixStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
        wr.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX);
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
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex((double) x, (double) (y + height), 0.0D).uv((u * f), ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.vertex((double) (x + width), (double) (y + height), 0.0D).uv(((u + (float) uWidth) * f), ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.vertex((double) (x + width), (double) y, 0.0D).uv(((u + (float) uWidth) * f), (v * f1)).endVertex();
        bufferbuilder.vertex((double) x, (double) y, 0.0D).uv((u * f), (v * f1)).endVertex();
        tessellator.end();
    }
}
