package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiButtonPair extends GuiButton {
    GuiButtonChat button1;
    GuiButtonChat button2;

    public boolean firstActiveButton;

    public GuiButtonPair(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText1, String buttonText2, boolean state)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText1);
        firstActiveButton = state;
        int buttWidth = widthIn / 2;
        button1 = new GuiButtonChat(1, x, y, buttWidth, heightIn, buttonText1);
        button2 = new GuiButtonChat(2, x + buttWidth, y, buttWidth, heightIn, buttonText2);
        if (firstActiveButton)
        {
            button2.setActive(false);
            button1.setActive(true);
        }
        else {
            button1.setActive(false);
            button2.setActive(true);
        }
    }

    @Override
    public void func_191745_a(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_) {
        button1.func_191745_a(p_191745_1_, p_191745_2_, p_191745_3_, p_191745_4_);
        button2.func_191745_a(p_191745_1_, p_191745_2_, p_191745_3_, p_191745_4_);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (firstActiveButton) {
            if (button2.mousePressed(mc, mouseX, mouseY)) {
                firstActiveButton = false;
                button1.setActive(false);
                button2.setActive(true);
                return true;
            }
        } else {
            if (button1.mousePressed(mc, mouseX, mouseY)) {
                firstActiveButton = true;
                button2.setActive(false);
                button1.setActive(true);
                return true;
            }
        }

        return false;
    }


}
