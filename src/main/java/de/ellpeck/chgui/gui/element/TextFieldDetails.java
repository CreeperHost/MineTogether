package de.ellpeck.chgui.gui.element;

import de.ellpeck.chgui.gui.GuiPersonalDetails;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class TextFieldDetails extends GuiTextField{

    private final GuiPersonalDetails gui;
    private final String displayString;
    private final boolean censorText;

    public TextFieldDetails(GuiPersonalDetails gui, int id, String displayString, String def, int x, int y, int width, int height){
        this(gui, id, displayString, def, x, y, width, height, false);
    }

    public TextFieldDetails(GuiPersonalDetails gui, int id, String displayString, String def, int x, int y, int width, int height, boolean censorText){
        super(id, gui.mc.fontRendererObj, x, y, width, height);
        this.gui = gui;
        this.displayString = displayString;
        this.censorText = censorText;
        this.setText(def);

        this.setMaxStringLength(64);
    }

    @Override
    public void drawTextBox(){
        if(this.censorText){
            String text = this.getText();

            char[] obscure = new char[text.length()];
            Arrays.fill(obscure, '*');
            this.setText(new String(obscure));

            super.drawTextBox();

            this.setText(text);
        }
        else{
            super.drawTextBox();
        }

        if(!this.isFocused() && this.getText().trim().isEmpty()){
            int x = this.xPosition+4;
            int y = this.yPosition+(this.height-8)/2;

            this.gui.mc.fontRendererObj.drawStringWithShadow(TextFormatting.ITALIC+this.displayString, x, y, 14737632);
        }
    }
}
