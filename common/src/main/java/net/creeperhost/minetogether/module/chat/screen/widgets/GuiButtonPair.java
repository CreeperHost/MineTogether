package net.creeperhost.minetogether.module.chat.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;

@SuppressWarnings("SuspiciousNameCombination")
public class GuiButtonPair extends Button
{
    GuiButtonChat button1;
    GuiButtonChat button2;
    
    ArrayList<GuiButtonChat> buttons = new ArrayList<>();
    
    public int activeButton;
    
    private final boolean stack;
    private final boolean swapOnClick;
    private final boolean vertical;
    
    public GuiButtonPair(int x, int y, int widthIn, int heightIn, int state, boolean stack, boolean swapOnClick, boolean vertical, Button.OnPress onPress, String... buttonTexts)
    {
        super(x, y, widthIn, heightIn, new TranslatableComponent(buttonTexts[0]), onPress);
        activeButton = state;
        this.swapOnClick = swapOnClick;
        this.stack = stack;
        this.vertical = vertical;
        
        for (String button : buttonTexts)
        {
            buttons.add(new GuiButtonChat(0, 0, 0, heightIn, button, onPress));
        }
        
        buttons.get(activeButton).setActive(true);
        
        button1 = buttons.get(0);
        button2 = buttons.get(1);
        
        setButtonDetails();
    }
    
    private void setButtonDetails()
    {
        int buttWidth = width / buttons.size();
        
        int baseX = x;
        int baseY = y;
        
        int buttonCount = buttons.size();
        
        for (int buttonNum = 0; buttonNum < buttonCount; buttonNum++)
        {
            GuiButtonChat button = buttons.get(buttonNum);
            int visibleNum = buttonNum;
            if (swapOnClick)
            {
                visibleNum = (buttonNum + buttonCount - activeButton) % buttonCount;
            }
            
            button.setWidth(buttWidth);
//            button.setHseeight(height);
            
            if (stack)
            {
                button.x = baseX;
                button.y = baseY + (visibleNum * height);
            } else
            {
                button.x = baseX + (visibleNum * buttWidth);
                button.y = baseY;
            }
        }
    }
    
    @Override
    public void render(PoseStack matrixStack, int p_191745_2_, int p_191745_3_, float p_191745_4_)
    {
        double mouseX = p_191745_2_;
        double mouseY = p_191745_3_;
        
        float scale = 0.75F;
        float xTranslate = -buttons.get(0).x;
        float yTranslate = -buttons.get(0).y;
        
        int buttonCount = buttons.size();
        
        int[] cachedX = new int[buttonCount];
        int[] cachedY = new int[buttonCount];
        
        float tempTranslateX;
        float tempTranslateY;
        
        if (vertical)
        {
            double xDiff = mouseX - button1.x;
            double yDiff = mouseY - button1.y;
            
            mouseX = yDiff / scale;
            mouseY = (xDiff / scale) + height;
            int buttWidth = width / buttons.size();
            
            for (int buttNum = 0; buttNum < buttonCount; buttNum++)
            {
                GuiButtonChat button = buttons.get(buttNum);
                int visibleNum = buttNum;
                if (swapOnClick)
                {
                    visibleNum = (buttNum + buttonCount - activeButton) % buttonCount;
                }
                cachedX[buttNum] = button.x;
                cachedY[buttNum] = button.y;
                button.x = buttWidth * visibleNum;
                button.y = 0;
            }
            
            RenderSystem.scaled(scale, scale, scale);
            
            RenderSystem.pushMatrix();
            
            if (stack)
            {
                tempTranslateX = -xTranslate + (height * 2);
                tempTranslateY = -yTranslate - width;
            } else
            {
                tempTranslateX = (-xTranslate * ((float) 1 / scale));
                tempTranslateY = (-yTranslate * ((float) 1 / scale));
            }
            
            tempTranslateY -= scale;
            
            RenderSystem.translated(tempTranslateX, tempTranslateY, 0);
            
            RenderSystem.rotatef(90, 0, 0, 1);
        }
        
        for (GuiButtonChat button : buttons)
        {
            button.render(matrixStack, (int) mouseX, (int) mouseY, p_191745_4_);
        }
        
        if (vertical)
        {
            RenderSystem.rotatef(90, 0, 0, -1);
            RenderSystem.popMatrix();
            
            RenderSystem.scalef(1.0F / scale, 1.0F / scale, 1.0F / scale);
            
            for (int buttNum = 0; buttNum < buttonCount; buttNum++)
            {
                GuiButtonChat button = buttons.get(buttNum);
                button.x = cachedX[buttNum];
                button.y = cachedY[buttNum];
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseXIn, double mouseYIn, int p_mouseClicked_5_)
    {
        double mouseX = mouseXIn;
        double mouseY = mouseYIn;
        if (vertical)
        {
            double xDiff = (mouseX - button1.x) / 0.75;
            double yDiff = (mouseY - button1.y) / 0.75;
            
            mouseX = button1.x + yDiff;
            mouseY = button1.y + xDiff + height;
        }
        
        boolean pressed = false;
        
        for (int buttonNum = 0; buttonNum < buttons.size(); buttonNum++)
        {
            GuiButtonChat button = buttons.get(buttonNum);
            activeButton = buttonNum;
            if (button.mouseClicked(mouseX, mouseY, p_mouseClicked_5_))
            {
                button.setActive(true);
                onPress();
                pressed = true;
            } else
            {
                button.setActive(false);
            }
        }
        return pressed;
    }
}
