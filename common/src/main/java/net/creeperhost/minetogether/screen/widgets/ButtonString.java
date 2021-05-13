package net.creeperhost.minetogether.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.helpers.ScreenHelpers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ButtonString extends Button
{
    public ButtonString(int i, int j, int k, int l, Component component, OnPress onPress)
    {
        super(i, j, k, l, component, onPress);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            this.renderBg(poseStack, mc, mouseX, mouseY);
            int color = 14737632;

            if(isHovered)
            {
                color = ChatFormatting.YELLOW.getColor();
                //TODO tooltips
//                Component tooltip = new TranslatableComponent("CLICK ME");
//                GuiComponent.drawCenteredString(poseStack, mc.font, tooltip, this.x + mc.font.width(tooltip), this.y - 5, -1);
            }

            GuiComponent.drawCenteredString(poseStack, mc.font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }
}