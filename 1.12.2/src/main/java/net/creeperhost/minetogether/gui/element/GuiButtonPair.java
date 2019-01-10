package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

@SuppressWarnings("SuspiciousNameCombination")
public class GuiButtonPair extends GuiButton
{
    GuiButtonChat button1;
    GuiButtonChat button2;
    
    public boolean firstActiveButton;
    
    private final boolean stack;
    private final boolean swapOnClick;
    private final boolean vertical;
    
    public GuiButtonPair(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText1, String buttonText2, boolean state, boolean stack, boolean swapOnClick, boolean vertical)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText1);
        firstActiveButton = state;
        this.swapOnClick = swapOnClick;
        this.stack = stack;
        this.vertical = vertical;
        
        button1 = new GuiButtonChat(1, 0, 0, 0, heightIn, buttonText1);
        button2 = new GuiButtonChat(2, 0, 0, 0, heightIn, buttonText2);
        if (firstActiveButton)
        {
            button2.setActive(false);
            button1.setActive(true);
        } else
        {
            button1.setActive(false);
            button2.setActive(true);
        }
        
        setButtonDetails();
    }
    
    private void setButtonDetails()
    {
        int buttWidth = width / 2;
        int button1X = xPosition;
        int button1Y = yPosition;
        int button2X = xPosition + buttWidth;
        int button2Y = yPosition;
        if (stack)
        {
            buttWidth = this.width;
            if (swapOnClick && firstActiveButton)
            {
                button1X = xPosition;
                button1Y = yPosition;
                button2X = xPosition;
                button2Y = yPosition + height;
            } else
            {
                button2X = xPosition;
                button2Y = yPosition;
                button1X = xPosition;
                button1Y = yPosition + height;
            }
        }

        /*if (vertical)
        {
            int oldButton1X = button1X;
            int oldButton2X = button2X;

            button1X = button1Y;
            button2X = button2Y;
            button1Y = oldButton1X;
            button2Y = oldButton2X;
        }*/
        
        button1.width = buttWidth;
        button1.height = height;
        button1.xPosition = button1X;
        button1.yPosition = button1Y;
        button2.width = buttWidth;
        button2.height = height;
        button2.xPosition = button2X;
        button2.yPosition = button2Y;
    }
    
    @Override
    public void func_191745_a(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_)
    {
        
        double mouseX = p_191745_2_;
        double mouseY = p_191745_3_;
        
        float scale = 0.75F;
        float xTranslate = (swapOnClick && firstActiveButton ? -button1.xPosition : -button2.xPosition);
        float yTranslate = (swapOnClick && firstActiveButton ? -button1.yPosition : -button2.yPosition);
        int oldButton1X = button1.xPosition;
        int oldButton1Y = button1.yPosition;
        int oldButton2X = button2.xPosition;
        int oldButton2Y = button2.yPosition;
        if (vertical)
        {
            mouseX = ((button1.xPosition < button2.xPosition ? button1.xPosition : button2.xPosition) - p_191745_2_) / 0.75;
            mouseY = (button1.yPosition - p_191745_3_) / 0.75;
            
            double x1 = mouseX - 0;
            double y1 = mouseY - 0;
            
            double angle = Math.toRadians(90);
            
            double x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
            double y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle);
            
            mouseX = x2 + width;
            mouseY = y2 + height - 4;
            
            if (swapOnClick && !firstActiveButton)
            {
                if (stack)
                {
                    button2.xPosition = 0;
                    button2.yPosition = 0;
                    button1.xPosition = 0;
                    button1.yPosition = height;
                } else
                {
                    button2.xPosition = 0;
                    button2.yPosition = 0;
                    button1.xPosition = width / 2;
                    button1.yPosition = 0;
                }
            } else
            {
                if (stack)
                {
                    button1.xPosition = 0;
                    button1.yPosition = 0;
                    button2.xPosition = 0;
                    button2.yPosition = height;
                } else
                {
                    button1.xPosition = 0;
                    button1.yPosition = 0;
                    button2.xPosition = width / 2;
                    button2.yPosition = 0;
                }
            }
            
            GlStateManager.scale(scale, scale, scale);
            
            GlStateManager.pushMatrix();
            
            if (stack)
                GlStateManager.translate(-xTranslate + (height * 2), -yTranslate - width, 0);
            else
                GlStateManager.translate((-xTranslate - (((width / 2) + 1.85) * scale)) / 3 * 4, (-yTranslate - (width * scale)) / 3 * 4, 0);
            
            GlStateManager.rotate(90, 0, 0, 1);
        }
        
        button1.func_191745_a(p_191745_1_, (int) mouseX, (int) mouseY, p_191745_4_);
        button2.func_191745_a(p_191745_1_, (int) mouseX, (int) mouseY, p_191745_4_);
        
        GlStateManager.rotate(90, 0, 0, -1);
        
        if (stack)
            GlStateManager.translate((-xTranslate + (height * 2)), (-yTranslate - width), 0);
        else
            GlStateManager.translate(-((-xTranslate - (((width / 2) + 1.85) * scale)) / 3 * 4), -((-yTranslate - (width * scale)) / 3 * 4), 0);
        
        
        if (vertical)
        {
            GlStateManager.popMatrix();
            
            GlStateManager.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);
            
            button1.xPosition = oldButton1X;
            button1.yPosition = oldButton1Y;
            button2.xPosition = oldButton2X;
            button2.yPosition = oldButton2Y;
        }
        
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseXIn, int mouseYIn)
    {
        
        double mouseX = mouseXIn;
        double mouseY = mouseYIn;
        if (vertical)
        {
            mouseX = ((button1.xPosition < button2.xPosition ? button1.xPosition : button2.xPosition) - mouseXIn) / 0.75;
            mouseY = (button1.yPosition - mouseYIn) / 0.75;
            
            double x1 = mouseX - 0;
            double y1 = mouseY - 0;
            
            double angle = Math.toRadians(90);
            
            double x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
            double y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle);
            
            mouseX = x2 + width;
            mouseY = y2 + height - 4;
            
            mouseX += button1.xPosition < button2.xPosition ? button1.xPosition : button2.xPosition;
            mouseY += button1.yPosition;
        }
        
        if (firstActiveButton)
        {
            if (button2.mousePressed(mc, (int) mouseX, (int) mouseY))
            {
                firstActiveButton = false;
                button1.setActive(false);
                button2.setActive(true);
                setButtonDetails();
                return true;
            }
        } else
        {
            if (button1.mousePressed(mc, (int) mouseX, (int) mouseY))
            {
                firstActiveButton = true;
                button2.setActive(false);
                button1.setActive(true);
                setButtonDetails();
                return true;
            }
        }
        
        return false;
    }
    
    
}
