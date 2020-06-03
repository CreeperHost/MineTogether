package net.creeperhost.minetogether.client.gui.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Aaron on 19/05/2017.
 */
public class GuiTextFieldValidate extends TextFieldWidget
{
    Pattern pattern;
    
    public GuiTextFieldValidate(FontRenderer p_i1032_1_, int p_i1032_2_, int p_i1032_3_, int p_i1032_4_, int p_i1032_5_, String regexStr, String s)
    {
        super(p_i1032_1_, p_i1032_2_, p_i1032_3_, p_i1032_4_, p_i1032_5_, s);
        pattern = Pattern.compile(regexStr);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void writeText(String textWrite)
    {
        int prevPos = this.getCursorPosition();
        String beforeWrite = this.getText();
        super.writeText(textWrite);
        String afterWrite = this.getText();
        Matcher matcher = pattern.matcher(afterWrite);
        if (!matcher.matches())
        {
            this.setText(beforeWrite);
            this.setCursorPosition(prevPos);
        }
    }
    
    @Override
    public void setText(String string)
    {
        Matcher matcher = pattern.matcher(string);
        if (matcher.matches())
        {
            super.setText(string);
        }
    }
}
