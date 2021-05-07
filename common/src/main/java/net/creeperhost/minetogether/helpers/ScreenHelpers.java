package net.creeperhost.minetogether.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Minetogether;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

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
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Minetogether.MOD_ID, "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(Minetogether.MOD_ID, "textures/minetogether25.png");

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
}
