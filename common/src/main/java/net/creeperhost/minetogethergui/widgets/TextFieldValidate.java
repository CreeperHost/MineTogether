package net.creeperhost.minetogethergui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Aaron on 19/05/2017.
 */
public class TextFieldValidate extends EditBox
{
    Pattern pattern;

    public TextFieldValidate(Font p_i1032_1_, int p_i1032_2_, int p_i1032_3_, int p_i1032_4_, int p_i1032_5_, String regexStr, String s)
    {
        super(p_i1032_1_, p_i1032_2_, p_i1032_3_, p_i1032_4_, p_i1032_5_, new TranslatableComponent(s));
        pattern = Pattern.compile(regexStr);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void insertText(String textWrite)
    {
        int prevPos = this.getCursorPosition();
        String beforeWrite = this.getValue();
        super.insertText(textWrite);
        String afterWrite = this.getValue();
        Matcher matcher = pattern.matcher(afterWrite);
        if (!matcher.matches())
        {
            this.setValue(beforeWrite);
            this.setCursorPosition(prevPos);
        }
    }
    
    @Override
    public void setValue(String string)
    {
        Matcher matcher = pattern.matcher(string);
        if (matcher.matches())
        {
            super.setValue(string);
        }
    }
}
