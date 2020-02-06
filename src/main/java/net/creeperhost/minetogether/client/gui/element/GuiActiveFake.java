package net.creeperhost.minetogether.client.gui.element;

import net.minecraft.client.gui.widget.button.Button;

public class GuiActiveFake extends Button
{
    public boolean active;

    public GuiActiveFake(int x, int y, int widthIn, int heightIn, String buttonText, Button.IPressable onPress)
    {
        super(x, y, widthIn, heightIn, buttonText, onPress);
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
    {
        if (active)
        {
            mouseX = x + 1;
            mouseY = y + 1;
        }
        return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
    }
}
