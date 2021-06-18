package net.creeperhost.minetogethergui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ButtonMultiple extends Button
{
    private static ResourceLocation buttonImg = new ResourceLocation(Constants.MOD_ID, "textures/widgets.png");
    private final int index;
    private Component tooltip;

    public ButtonMultiple(int xPos, int yPos, int index, OnPress onPress)
    {
        super(xPos, yPos, 20, 20, new TranslatableComponent(""), onPress);
        this.index = index;
        this.tooltip = new TranslatableComponent("");
    }

    public ButtonMultiple(int xPos, int yPos, int index, Component tooltip, OnPress onPress)
    {
        super(xPos, yPos, 20, 20, new TranslatableComponent(""), onPress);
        this.index = index;
        this.tooltip = tooltip;
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
            ResourceLocation buttonImage = buttonImg;
            Minecraft.getInstance().getTextureManager().bind(buttonImage);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(poseStack, this.x, this.y, index * 20, getY(), this.width, this.height);
        }
    }

    public Component getTooltip()
    {
        return tooltip;
    }
}
