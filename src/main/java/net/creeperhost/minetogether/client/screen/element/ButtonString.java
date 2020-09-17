package net.creeperhost.minetogether.client.screen.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ButtonString extends Button
{
    public ButtonString(int xPos, int yPos, int width, int height, String displayString, Button.IPressable handler)
    {
        super(xPos, yPos, width, height, new StringTextComponent(displayString), handler);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            this.renderBg(matrixStack, mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColor != 0)
            {
                color = packedFGColor;
            } else if (!this.active)
            {
                color = 10526880;
            } else if (this.isHovered)
            {
                color = 16777120;
            }

//            ITextComponent buttonText = this.getMessage();
//            int strWidth = mc.fontRenderer.getStringWidth(buttonText.getString());
//            int ellipsisWidth = mc.fontRenderer.getStringWidth("...");
//
//            if (strWidth > width - 6 && strWidth > ellipsisWidth)
//                buttonText = mc.fontRenderer.trimStringToWidth(buttonText, width - 6 - ellipsisWidth).trim() + "...";

            this.drawCenteredString(matrixStack, mc.fontRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }
}
