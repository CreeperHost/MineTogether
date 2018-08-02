package net.creeperhost.minetogether.gui.element;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.gui.chat.ingame.GuiNewChatOurs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

public class GuiButtonChat extends GuiButton {

    private boolean active;

    public GuiButtonChat(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public void func_191745_a(Minecraft p_191745_1_, int mouseX, int mouseY, float p_191745_4_)
    {
        if (active) {
            mouseX = xPosition + 1;
            mouseY = yPosition + 1;
        }

        if (this.visible)
        {
            FontRenderer fontrenderer = p_191745_1_.fontRendererObj;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            int j = 0xFFFFFF;

            int l1 = 200;

            if (((GuiNewChatOurs)Minecraft.getMinecraft().ingameGUI.getChatGUI()).unread)
            {
                j = fontrenderer.getColorCode('b');
            }

            if (this.hovered)
            {
                j = 16777120;
                l1 = 256;
            }

            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, l1 / 2 << 24);

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);

        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean ret = super.mousePressed(mc, mouseX, mouseY);
        if (ret)
            ((GuiNewChatOurs)Minecraft.getMinecraft().ingameGUI.getChatGUI()).unread = false;

        return ret;
    }
}
