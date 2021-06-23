package net.creeperhost.minetogethergui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ButtonMultiple extends Button
{
    private final ResourceLocation resourceLocation;
    private final int index;
    private final Component tooltip;

    public ButtonMultiple(int xPos, int yPos, int index, ResourceLocation resourceLocation, OnPress onPress)
    {
        super(xPos, yPos, 20, 20, new TranslatableComponent(""), onPress);
        this.index = index;
        this.tooltip = new TranslatableComponent("");
        this.resourceLocation = resourceLocation;
    }

    public ButtonMultiple(int xPos, int yPos, int index, ResourceLocation resourceLocation, Component tooltip, OnPress onPress)
    {
        super(xPos, yPos, 20, 20, new TranslatableComponent(""), onPress);
        this.index = index;
        this.tooltip = tooltip;
        this.resourceLocation = resourceLocation;
    }

    public int getY()
    {
        if(!active) return 40;
        return isHovered ? this.height : 0;
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f)
    {
        if (this.visible)
        {
            Minecraft.getInstance().getTextureManager().bind(resourceLocation);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(poseStack, this.x, this.y, index * 20, getY(), this.width, this.height);
        }
    }

    public Component getTooltip()
    {
        return tooltip;
    }
}
