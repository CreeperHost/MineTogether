package de.ellpeck.chgui.gui;

import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.gui.element.TextFieldDetails;
import de.ellpeck.chgui.paul.Order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiPersonalDetails extends GuiGetServer{

    public final List<TextFieldDetails> fields = new ArrayList<TextFieldDetails>();

    public GuiPersonalDetails(int stepId, Order order){
        super(stepId, order);
    }

    @Override
    public String getStepName(){
        return Util.localize("gui.personal_details");
    }

    @Override
    public void initGui(){
        super.initGui();

        int x = this.width/2;

        this.fields.add(new TextFieldDetails(this, 0, Util.localize("info.e_mail"), this.order.emailAddress, x-205, 55, 200, 20));
        this.fields.add(new TextFieldDetails(this, 1, Util.localize("info.password"), this.order.password, x+5, 55, 200, 20, true));

        this.fields.add(new TextFieldDetails(this, 2, Util.localize("info.first_name"), this.order.firstName, x-205, 85, 200, 20));
        this.fields.add(new TextFieldDetails(this, 3, Util.localize("info.last_name"), this.order.lastName, x+5, 85, 200, 20));

        this.fields.add(new TextFieldDetails(this, 4, Util.localize("info.address"), this.order.address, x-205, 115, 200, 20));
        this.fields.add(new TextFieldDetails(this, 5, Util.localize("info.city"), this.order.city, x+5, 115, 200, 20));

        this.fields.add(new TextFieldDetails(this, 6, Util.localize("info.zip"), this.order.zip, x-205, 145, 200, 20));
        this.fields.add(new TextFieldDetails(this, 7, Util.localize("info.state"), this.order.state, x+5, 145, 200, 20));

        this.fields.add(new TextFieldDetails(this, 8, Util.localize("info.country"), this.order.country, x-205, 175, 200, 20));
        this.fields.add(new TextFieldDetails(this, 9, Util.localize("info.phone"), this.order.phone, x+5, 175, 200, 20));
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        this.buttonNext.enabled = true;

        for(TextFieldDetails field : this.fields){
            field.updateCursorCounter();

            if(field.getText().trim().isEmpty()){
                this.buttonNext.enabled = false;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException{
        for(TextFieldDetails field : this.fields){
            if(field.textboxKeyTyped(typedChar, keyCode)){
                int id = field.getId();
                String text = field.getText().trim();

                if(id == 0){
                    this.order.emailAddress = text;
                }
                else if(id == 1){
                    this.order.password = text;
                }
                else if(id == 2){
                    this.order.firstName = text;
                }
                else if(id == 3){
                    this.order.lastName = text;
                }
                else if(id == 4){
                    this.order.address = text;
                }
                else if(id == 5){
                    this.order.city = text;
                }
                else if(id == 6){
                    this.order.zip = text;
                }
                else if(id == 7){
                    this.order.state = text;
                }
                else if(id == 8){
                    this.order.country = text;
                }
                else if(id == 9){
                    this.order.phone = text;
                }

                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        for(TextFieldDetails field : this.fields){
            field.drawTextBox();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for(TextFieldDetails field : this.fields){
            field.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
