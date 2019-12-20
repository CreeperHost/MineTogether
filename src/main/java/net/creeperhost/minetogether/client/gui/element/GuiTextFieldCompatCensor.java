package net.creeperhost.minetogether.client.gui.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiTextFieldCompatCensor extends TextFieldWidget
{
    public GuiTextFieldCompatCensor(FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, String s)
    {
        super(fontrendererObj, x, y, par5Width, par6Height, s);
    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_)
    {
        super.render(p_render_1_, p_render_2_, p_render_3_);
    }

//    @Override
//    public void drawTextBox()
//    {
//        String oldText = getText();
//        int oldCursor = getCursorPosition();
//        String censorText = new String(new char[oldText.length()]).replace("\0", "*");
//        setText(censorText);
//        super.drawTextBox();
//        setText(oldText);
//        setCursorPosition(oldCursor);
//    }
}
