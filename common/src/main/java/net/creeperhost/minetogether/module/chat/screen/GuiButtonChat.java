package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;

public class GuiButtonChat extends Button
{
    private boolean active;
    String buttonText;
    
    public GuiButtonChat(int x, int y, int widthIn, int heightIn, String buttonText, Button.OnPress onPress)
    {
        super(x, y, widthIn, heightIn, new TranslatableComponent(buttonText), onPress);
        this.buttonText = buttonText;
    }
    
    public void setActive(boolean active)
    {
        this.active = active;
    }
    
    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float p_191745_4_)
    {
        if (active)
        {
            mouseX = x + 1;
            mouseY = y + 1;
        }
        
        if (this.visible)
        {
            Font fontrenderer = Minecraft.getInstance().font;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            
            int j = 0xFFFFFF;
            
            int l1 = 200;
            
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
//        if (ret)
//            if (MineTogether.instance.gdpr.hasAcceptedGDPR())
//                ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).unread = false;
        
        return ret;
    }
}
