package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiActiveFake extends GuiButton {

    private boolean active;

    public GuiActiveFake(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public void func_191745_a(Minecraft p_191745_1_, int mouseX, int mouseY, float p_191745_4_) {
        if (active) {
            mouseX = xPosition + 1;
            mouseY = yPosition + 1;
        }
        super.func_191745_a(p_191745_1_, mouseX, mouseY, p_191745_4_);
    }
}
