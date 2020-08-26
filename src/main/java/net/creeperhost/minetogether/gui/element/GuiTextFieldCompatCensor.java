package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.gui.FontRenderer;

public class GuiTextFieldCompatCensor extends GuiTextFieldCompat
{
    public GuiTextFieldCompatCensor(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height)
    {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }
    
    @Override
    public void drawTextBox()
    {
        String oldText = getText();
        int oldCursor = getCursorPosition();
        String censorText = new String(new char[oldText.length()]).replace("\0", "*");
        setText(censorText);
        super.drawTextBox();
        setText(oldText);
        setCursorPosition(oldCursor);
    }
}
