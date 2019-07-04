package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;

@SuppressWarnings("SuspiciousNameCombination")
public class GuiButtonPair extends GuiButton
{
    GuiButtonChat button1;
    GuiButtonChat button2;

    ArrayList<GuiButtonChat> buttons = new ArrayList<>();

    public int activeButton;

    private final boolean stack;
    private final boolean swapOnClick;
    private final boolean vertical;

    public GuiButtonPair(int buttonId, int x, int y, int widthIn, int heightIn, int state, boolean stack, boolean swapOnClick, boolean vertical, String ... buttonTexts)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonTexts[0]);
        activeButton = state;
        this.swapOnClick = swapOnClick;
        this.stack = stack;
        this.vertical = vertical;

        int fakeButtonId = 1;

        for(String button: buttonTexts)
        {
            buttons.add(new GuiButtonChat(fakeButtonId++, 0, 0, 0, heightIn, button));
        }

        buttons.get(activeButton).setActive(true);

        button1 = buttons.get(0);
        button2 = buttons.get(1);

        /*for(int buttonNum = 0; buttonNum < buttons.size(); buttonNum++)
        {

        }

        if (activeButton == 0)
        {
            button2.setActive(false);
            button1.setActive(true);
        } else
        {
            button1.setActive(false);
            button2.setActive(true);
        }*/

        setButtonDetails();
    }

    private void setButtonDetails()
    {
        int buttWidth = width / buttons.size();

        int baseX = xPosition;
        int baseY = yPosition;

        int buttonCount = buttons.size();

        for(int buttonNum = 0; buttonNum < buttonCount; buttonNum++)
        {
            GuiButtonChat button = buttons.get(buttonNum);
            int visibleNum = buttonNum;
            if (swapOnClick)
            {
                visibleNum = (buttonNum + buttonCount - activeButton) % buttonCount;
            }

            button.width = buttWidth;
            button.height = height;

            if (stack)
            {
                button.xPosition = baseX;
                button.yPosition = baseY + (visibleNum * height);
            } else {
                button.xPosition = baseX + (visibleNum * buttWidth);
                button.yPosition = baseY;
            }
        }
    }
    
    @Override
    public void func_191745_a(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_)
    {
        
        double mouseX = p_191745_2_;
        double mouseY = p_191745_3_;
        
        float scale = 0.75F;
        float xTranslate = (swapOnClick && activeButton == 0 ? -button1.xPosition : -button2.xPosition);
        float yTranslate = (swapOnClick && activeButton == 0 ? -button1.yPosition : -button2.yPosition);
        int oldButton1X = button1.xPosition;
        int oldButton1Y = button1.yPosition;
        int oldButton2X = button2.xPosition;
        int oldButton2Y = button2.yPosition;

        int buttonCount = buttons.size();

        int[] cachedX = new int[buttonCount];
        int[] cachedY = new int[buttonCount];

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

            //if (swapOnClick && activeButton != 0)
            {
                int buttWidth = width / buttons.size();

                for(int buttNum = 0; buttNum < buttonCount; buttNum++)
                {
                    GuiButtonChat button = buttons.get(buttNum);
                    int visibleNum = buttNum;
                    if (swapOnClick)
                    {
                        visibleNum = (buttNum + buttonCount - activeButton) % buttonCount;
                    }
                    cachedX[buttNum] = button.xPosition;
                    cachedY[buttNum] = button.yPosition;
                    button.xPosition = buttWidth * visibleNum;
                    button.yPosition = 0;
                }


                /*if (stack)
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
                }*/
            }
            
            GlStateManager.scale(scale, scale, scale);
            
            GlStateManager.pushMatrix();
            
            if (stack)
                GlStateManager.translate(-xTranslate + (height * 2), -yTranslate - width, 0);
            else
                GlStateManager.translate((-xTranslate - (((width / 2) + 1.85) * scale)) / 3 * 4, (-yTranslate - (width * scale)) / 3 * 4, 0);
            
            GlStateManager.rotate(90, 0, 0, 1);
        }

        for(GuiButtonChat button: buttons)
        {
            button.func_191745_a(p_191745_1_, (int) mouseX, (int) mouseY, p_191745_4_);
        }

        GlStateManager.rotate(90, 0, 0, -1);
        
        if (stack)
            GlStateManager.translate((-xTranslate + (height * 2)), (-yTranslate - width), 0);
        else
            GlStateManager.translate(-((-xTranslate - (((width / 2) + 1.85) * scale)) / 3 * 4), -((-yTranslate - (width * scale)) / 3 * 4), 0);
        
        
        if (vertical)
        {
            GlStateManager.popMatrix();
            
            GlStateManager.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);

            for(int buttNum = 0; buttNum < buttonCount; buttNum++) {
                GuiButtonChat button = buttons.get(buttNum);
                button.xPosition = cachedX[buttNum];
                button.yPosition = cachedY[buttNum];
            }
            
            /*button1.xPosition = oldButton1X;
            button1.yPosition = oldButton1Y;
            button2.xPosition = oldButton2X;
            button2.yPosition = oldButton2Y;*/
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
            mouseY = ((button1.yPosition - mouseYIn) / 0.75);

            double x1 = mouseX +8;
            double y1 = mouseY - 0;

            double angle = Math.toRadians(90);

            double x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
            double y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle);

            mouseX = x2 + width;
            mouseY = y2 + height - 4;

            mouseX += button1.xPosition < button2.xPosition ? button1.xPosition : button2.xPosition;
            mouseY += button1.yPosition;
        }

        boolean pressed = false;

        for(int buttonNum = 0; buttonNum < buttons.size(); buttonNum++)
        {
            GuiButtonChat button = buttons.get(buttonNum);
            if (button.mousePressed(mc, (int) mouseX, (int) mouseY))
            {
                activeButton = buttonNum;
                button.setActive(true);
                pressed = true;
            } else {
                button.setActive(false);
            }
        }

        System.out.println(pressed);

        return pressed;

        /*if (activeButton == 0)
        {
            if (button2.mousePressed(mc, (int) mouseX, (int) mouseY))
            {
                activeButton = 1;
                button1.setActive(false);
                button2.setActive(true);
                setButtonDetails();
                return true;
            }
        } else
        {
            if (button1.mousePressed(mc, (int) mouseX, (int) mouseY))
            {
                activeButton = 0;
                button2.setActive(false);
                button1.setActive(true);
                setButtonDetails();
                return true;
            }
        }*/

        //return false;
    }
}
