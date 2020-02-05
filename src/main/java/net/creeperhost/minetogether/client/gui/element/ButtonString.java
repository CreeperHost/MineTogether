package net.creeperhost.minetogether.client.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class ButtonString extends GuiButtonExt
{
    public ButtonString(int xPos, int yPos, int width, int height, String displayString, IPressable handler)
    {
        super(xPos, yPos, width, height, displayString, handler);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            this.renderBg(mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColor != 0)
            {
                color = packedFGColor;
            }
            else if (!this.active)
            {
                color = 10526880;
            }
            else if (this.isHovered)
            {
                color = 16777120;
            }

            String buttonText = this.getMessage();
            int strWidth = mc.fontRenderer.getStringWidth(buttonText);
            int ellipsisWidth = mc.fontRenderer.getStringWidth("...");

            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                buttonText = mc.fontRenderer.trimStringToWidth(buttonText, width - 6 - ellipsisWidth).trim() + "...";

            this.drawCenteredString(mc.fontRenderer, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }
}
