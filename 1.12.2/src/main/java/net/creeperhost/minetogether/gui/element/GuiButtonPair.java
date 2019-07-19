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

            System.out.println(button);
        }
    }
    
    @Override
    public void func_191745_a(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_)
    {
        
        double mouseX = p_191745_2_;
        double mouseY = p_191745_3_;
        
        float scale = 0.75F;
        float xTranslate = -buttons.get(0).xPosition;
        float yTranslate = -buttons.get(0).yPosition;

        int buttonCount = buttons.size();

        int[] cachedX = new int[buttonCount];
        int[] cachedY = new int[buttonCount];

        float tempTranslateX;
        float tempTranslateY;

        if (vertical)
        {
            double xDiff = mouseX - button1.xPosition;
            double yDiff = mouseY - button1.yPosition;

            mouseX = yDiff / scale;
            mouseY = (xDiff / scale) + height;

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

            }
            
            GlStateManager.scale(scale, scale, scale);
            
            GlStateManager.pushMatrix();

            if (stack) {
                tempTranslateX = -xTranslate + (height * 2);
                tempTranslateY = -yTranslate - width;
            } else {
                tempTranslateX = (-xTranslate * ((float)1 / scale));
                tempTranslateY = (-yTranslate * ((float)1 / scale));
            }

            //tempTranslateX += 3;
            tempTranslateY -= scale;

            GlStateManager.translate(tempTranslateX, tempTranslateY, 0);
            
            GlStateManager.rotate(90, 0, 0, 1);
        }

        for(GuiButtonChat button: buttons)
        {
            button.func_191745_a(p_191745_1_, (int) mouseX, (int) mouseY, p_191745_4_);
        }

        if (vertical)
        {
            GlStateManager.rotate(90, 0, 0, -1);
            GlStateManager.popMatrix();
            
            GlStateManager.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);

            for(int buttNum = 0; buttNum < buttonCount; buttNum++) {
                GuiButtonChat button = buttons.get(buttNum);
                button.xPosition = cachedX[buttNum];
                button.yPosition = cachedY[buttNum];
            }

        }
        
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseXIn, int mouseYIn)
    {

        double mouseX = mouseXIn;
        double mouseY = mouseYIn;
        if (vertical)
        {
            double xDiff = (mouseX - button1.xPosition) / 0.75;
            double yDiff = (mouseY - button1.yPosition) / 0.75;

            mouseX = button1.xPosition + yDiff;
            mouseY = button1.yPosition + xDiff + height;
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
