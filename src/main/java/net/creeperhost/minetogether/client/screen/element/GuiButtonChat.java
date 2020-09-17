package net.creeperhost.minetogether.client.screen.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.client.screen.chat.ingame.GuiNewChatOurs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class GuiButtonChat extends Button
{
    private boolean active;
    String buttonText;
    
    public GuiButtonChat(int x, int y, int widthIn, int heightIn, String buttonText, Button.IPressable onPress)
    {
        super(x, y, widthIn, heightIn, new StringTextComponent(buttonText), onPress);
        this.buttonText = buttonText;
    }
    
    public void setActive(boolean active)
    {
        this.active = active;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float p_191745_4_)
    {
        if (active)
        {
            mouseX = x + 1;
            mouseY = y + 1;
        }
        
        if (this.visible)
        {
            FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            
            int j = 0xFFFFFF;
            
            int l1 = 200;
            
            if (MineTogether.instance.gdpr.hasAcceptedGDPR() && ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).unread)
            {
//                j = fontrenderer.getColorCode('b');
            }
            
            if (this.isHovered)
            {
                j = 16777120;
                l1 = 256;
            }
            
            fill(matrixStack, x, y, x + width, y + height, l1 / 2 << 24);
            
            this.drawCenteredString(matrixStack, fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
            
        }
    }
    
    
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
