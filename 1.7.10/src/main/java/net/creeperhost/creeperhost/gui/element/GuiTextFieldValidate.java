package net.creeperhost.creeperhost.gui.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Aaron on 19/05/2017.
 */
public class GuiTextFieldValidate extends GuiTextField
{
    Pattern pattern;
    public GuiTextFieldValidate(FontRenderer p_i1032_1_, int p_i1032_2_, int p_i1032_3_, int p_i1032_4_, int p_i1032_5_, String regexStr)
    {
        super(p_i1032_1_, p_i1032_2_, p_i1032_3_, p_i1032_4_, p_i1032_5_);
        pattern = Pattern.compile(regexStr);
    }

    @Override
    public void writeText(String textWrite)
    {
        int prevPos = this.getCursorPosition();
        String beforeWrite = this.getText();
        super.writeText(textWrite);
        String afterWrite = this.getText();
        Matcher matcher = pattern.matcher(afterWrite);
        if (!matcher.matches()) {
            this.setText(beforeWrite);
            this.setCursorPosition(prevPos);
        }
    }

    @Override
    public void setText(String p_146180_1_)
    {
        Matcher matcher = pattern.matcher(p_146180_1_);
        if (matcher.matches()) {
            super.setText(p_146180_1_);
        }
    }


}
