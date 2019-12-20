package net.creeperhost.minetogether.client.gui.serverlist.gui.elements;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiButtonLarge extends GuiButtonExt
{
    private String description;
    private ItemStack stack;

    public GuiButtonLarge(int x, int y, int widthIn, int heightIn, String buttonText, String description, ItemStack stack, Button.IPressable onPress)
    {
        super(x, y, widthIn, heightIn, buttonText, onPress);
        this.width = 200;
        this.height = 20;
        this.visible = true;
        this.active = true;
        this.x = x;
        this.y = y;
        this.width = widthIn;
        this.height = heightIn;
        this.setMessage(buttonText);
        this.description = description;
        this.stack = stack;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = this.getYImage(this.isHovered);
            GuiUtils.drawContinuousTexturedBox(WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.blitOffset);
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
//            RenderItem renderItem = Minecraft.getInstance().getRenderItem();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(2.0f, 2.0f, 2.0f);
//            renderItem.renderItemAndEffectIntoGUI(stack, (this.xPosition / 2) + (width / 4) - 8, (this.yPosition / 2) + 10);
            //renderItem.renderItemIntoGUI(stack, (this.xPosition / 2) + (width / 4) - 8, (this.yPosition / 2) + 10);
            GlStateManager.popMatrix();
        }
    }

    public static String padLeft(String s, int n)
    {
        return String.format("%1$" + n + "s", s);
    }
}
