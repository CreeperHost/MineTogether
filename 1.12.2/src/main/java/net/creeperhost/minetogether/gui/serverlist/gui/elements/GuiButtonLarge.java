package net.creeperhost.minetogether.gui.serverlist.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GuiButtonLarge extends GuiButtonExt
{
    private String description;
    private ItemStack stack;

    public GuiButtonLarge(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, buttonText);
    }

    public GuiButtonLarge(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String description, ItemStack stack)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
        this.description = description;
        this.stack = stack;
    }

    public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float p_191745_4_)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int k = this.getHoverState(this.hovered);
            GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, this.xPosition, this.yPosition, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColour != 0) {
                color = packedFGColour;
            } else if (!this.enabled) {
                color = 10526880;
            } else if (this.hovered) {
                color = 16777120;
            }

            this.drawCenteredString(mc.fontRendererObj, TextFormatting.BOLD + this.displayString, this.xPosition + this.width / 2, this.yPosition + (+8), color);

            List<ITextComponent> newstring = GuiUtilRenderComponents.splitText(new TextComponentString(description), width - 10, mc.fontRendererObj, false, true);
            int start = 105;

            for (ITextComponent s : newstring) {
                int left = ((this.xPosition + 4));
                mc.fontRendererObj.drawStringWithShadow(padLeft(s.getFormattedText(), 20), left, start += 8, -1);
            }
            try {
                RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.0f, 2.0f, 2.0f);
                renderItem.renderItemIntoGUI(stack, (this.xPosition / 2) + (width / 4) - 8, (this.yPosition / 2) + 10);
                GlStateManager.popMatrix();
            } catch(Exception err)
            {}
        }
    }

    public static String padLeft(String s, int n)
    {
        return String.format("%1$" + n + "s", s);
    }
}
