package net.creeperhost.minetogether.module.chat.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class GuiButtonPair extends Button
{
    GuiButtonChat button1;
    GuiButtonChat button2;
    GuiButtonChat button3;

    ArrayList<GuiButtonChat> buttons = new ArrayList<>();

    public int activeButton;

    private final boolean stack;
    private final boolean swapOnClick;
    private final boolean vertical;

    public GuiButtonPair(int x, int y, int widthIn, int heightIn, int state, boolean stack, boolean swapOnClick, boolean vertical, Button.OnPress onPress, String... buttonTexts)
    {
        super(x, y, widthIn, heightIn, Component.translatable(buttonTexts[0]), onPress);
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

    public ArrayList<GuiButtonChat> getButtons()
    {
        return buttons;
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

            if (stack)
            {
                button.x = baseX;
                button.y = baseY + (visibleNum * height);
            }
            else
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

            matrixStack.scale(scale, scale, scale);

            matrixStack.pushPose();

            if (stack)
            {
                tempTranslateX = -xTranslate + (height * 2);
                tempTranslateY = -yTranslate - width;
            }
            else
            {
                tempTranslateX = (-xTranslate * ((float) 1 / scale));
                tempTranslateY = (-yTranslate * ((float) 1 / scale));
            }

            tempTranslateY -= scale;

            matrixStack.translate(tempTranslateX, tempTranslateY, 0);

            matrixStack.mulPose(new Quaternion(-10, 0, 90, true));
        }

        for (GuiButtonChat button : buttons)
        {
            button.render(matrixStack, (int) mouseX, (int) mouseY, p_191745_4_);
        }

        if (vertical)
        {
            matrixStack.mulPose(new Quaternion(-10, 0, 90, true));
            matrixStack.popPose();

            matrixStack.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);

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
            if (button.mouseClicked(mouseX, mouseY, p_mouseClicked_5_))
            {
                if (activeButton != buttonNum) buttons.get(activeButton).setActive(false);

                activeButton = buttonNum;
                button.setActive(true);
                onPress();
                pressed = true;
                break;
            }
        }
        return pressed;
    }
}
