package net.creeperhost.minetogethergui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.creeperhost.minetogether.MineTogether;
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
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(MineTogether.MOD_ID, "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(MineTogether.MOD_ID, "textures/minetogether25.png");

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

    protected static void fillGradient(Matrix4f matrix4f, BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o) {
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

    public static void renderGif(PoseStack poseStack, ResourceLocation resourceLocation, int x, int y)
    {
        GlStateManager._color4f(1, 1, 1, 1);
    }
}
