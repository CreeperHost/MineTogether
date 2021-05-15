package net.creeperhost.minetogethergui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
            }

            GuiComponent.drawCenteredString(poseStack, mc.font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }
}
