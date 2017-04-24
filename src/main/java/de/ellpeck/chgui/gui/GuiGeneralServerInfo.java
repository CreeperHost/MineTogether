package de.ellpeck.chgui.gui;

import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.paul.Constants;
import de.ellpeck.chgui.paul.Order;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

public class GuiGeneralServerInfo extends GuiGetServer implements GuiPageButtonList.GuiResponder{

    private static final GuiSlider.FormatHelper SLIDER_FORMATTER = new GuiSlider.FormatHelper(){
        @Override
        public String getText(int id, String name, float value){
            return name+": "+(int)value;
        }
    };

    private GuiTextField nameField;
    private GuiSlider slotSlider;

    public GuiGeneralServerInfo(int stepId, Order order){
        super(stepId, order);
    }

    @Override
    public void initGui(){
        super.initGui();

        int halfWidth = this.width/2;
        int halfHeight = this.height/2;

        this.nameField = new GuiTextField(0, this.fontRendererObj, halfWidth-100, halfHeight-30, 200, 20);
        this.nameField.setMaxStringLength(Constants.MAX_SERVER_NAME_LENGTH);
        this.nameField.setText(this.order.name);

        this.slotSlider = new GuiSlider(this, 1, halfWidth-100, halfHeight+20, Util.localize("slider.player_count"), Constants.MIN_PLAYER_COUNT, Constants.MAX_PLAYER_COUNT, this.order.playerAmount, SLIDER_FORMATTER);
        this.slotSlider.width = 200;
        this.buttonList.add(this.slotSlider);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        this.nameField.updateCursorCounter();

        this.buttonNext.enabled = !this.nameField.getText().trim().isEmpty();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException{
        if(!this.nameField.textboxKeyTyped(typedChar, keyCode)){
            super.keyTyped(typedChar, keyCode);
        }
        else{
            this.order.name = this.nameField.getText().trim();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRendererObj, Util.localize("info.server_name"), this.width/2, this.height/2-45, -1);

        this.nameField.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public String getStepName(){
        return Util.localize("gui.general_info");
    }

    @Override
    public void setEntryValue(int id, boolean value){

    }

    @Override
    public void setEntryValue(int id, float value){
        this.order.playerAmount = (int)value;
    }

    @Override
    public void setEntryValue(int id, String value){

    }
}
