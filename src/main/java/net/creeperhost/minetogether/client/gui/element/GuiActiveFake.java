package net.creeperhost.minetogether.client.gui.element;

import net.minecraft.client.gui.widget.button.Button;

public class GuiActiveFake extends Button
{
    private boolean active;
    
    public GuiActiveFake(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Button.IPressable onPress)
    {
        super(x, y, widthIn, heightIn, buttonText, onPress);
    }
    
    public void setActive(boolean active)
    {
        this.active = active;
    }

    //TODO no idea what this does
//    @Override
//    public void func_191745_a(Minecraft p_191745_1_, int mouseX, int mouseY, float p_191745_4_)
//    {
//        if (active)
//        {
//            mouseX = xPosition + 1;
//            mouseY = yPosition + 1;
//        }
//        super.func_191745_a(p_191745_1_, mouseX, mouseY, p_191745_4_);
//    }
}
