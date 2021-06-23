package net.creeperhost.minetogethergui.screenbuilder;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class ScreenBuilder
{
    private final ResourceLocation resourceLocation;

    public ScreenBuilder(ResourceLocation resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    public void drawDefaultBackground(ContainerScreen screen, PoseStack poseStack, int x, int y, int width, int height, int textureXSize, int textureYSize)
    {
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        GuiComponent.blit(poseStack, x, y, 0, 0, width / 2, height / 2, textureXSize, textureYSize);
        GuiComponent.blit(poseStack, x + width / 2, y, 150 - width / 2, 0, width / 2, height / 2, textureXSize, textureYSize );
        GuiComponent.blit(poseStack, x, y + height / 2, 0, 150 - height / 2, width / 2, height / 2, textureXSize, textureYSize);
        GuiComponent.blit(poseStack, x + width / 2, y + height / 2, 150 - width / 2, 150 - height / 2, width / 2, height / 2, textureXSize, textureYSize);
    }

    public void drawPlayerSlots(ContainerScreen screen, PoseStack poseStack, int posX, int posY, boolean center, int textureXSize, int textureYSize)
    {
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        if (center)
        {
            posX -= 81;
        }
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                GuiComponent.blit(poseStack, posX + x * 18, posY + y * 18, 150, 0, 18, 18, textureXSize, textureYSize);
            }
        }
        for (int x = 0; x < 9; x++)
        {
            GuiComponent.blit(poseStack, posX + x * 18, posY + 58, 150, 0, 18, 18, textureXSize, textureYSize);
        }
    }

    public void drawSlot(ContainerScreen gui, PoseStack poseStack, int posX, int posY, int textureXSize, int textureYSize)
    {
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        GuiComponent.blit(poseStack, posX, posY, 150, 0, 18, 18, textureXSize, textureYSize);
    }

    public boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY)
    {
        return ((mouseX >= x && mouseX <= x + xSize) && (mouseY >= y && mouseY <= y + ySize));
    }
}
