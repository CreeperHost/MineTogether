package net.creeperhost.minetogether.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

public class RenderUtils
{
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
        itemRenderer.renderItemAndEffectIntoGUI(stack, -8, -8);

        RenderSystem.popMatrix();
    }
}
