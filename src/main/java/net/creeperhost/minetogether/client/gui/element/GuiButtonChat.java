package net.creeperhost.minetogether.client.gui.element;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.client.gui.chat.ingame.GuiNewChatOurs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class GuiButtonChat extends Button
{
    private boolean active;
    
    public GuiButtonChat(int x, int y, int widthIn, int heightIn, String buttonText, Button.IPressable onPress)
    {
        super(x, y, widthIn, heightIn, buttonText, onPress);
    }
    
    public void setActive(boolean active)
    {
        this.active = active;
    }

//    @Override
//    public void func_191745_a(Minecraft p_191745_1_, int mouseX, int mouseY, float p_191745_4_)
//    {
//        if (active)
//        {
//            mouseX = xPosition + 1;
//            mouseY = yPosition + 1;
//        }
//
//        if (this.visible)
//        {
//            FontRenderer fontrenderer = p_191745_1_.fontRendererObj;
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
//
//            int j = 0xFFFFFF;
//
//            int l1 = 200;
//
//            if (CreeperHost.instance.gdpr.hasAcceptedGDPR() && ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).unread)
//            {
//                j = fontrenderer.getColorCode('b');
//            }
//
//            if (this.hovered)
//            {
//                j = 16777120;
//                l1 = 256;
//            }
//
//            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, l1 / 2 << 24);
//
//            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
//
//        }
//    }
    
    
    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        boolean ret = super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if (ret)
            if (MineTogether.instance.gdpr.hasAcceptedGDPR())
                ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).unread = false;
        
        return ret;
    }
}