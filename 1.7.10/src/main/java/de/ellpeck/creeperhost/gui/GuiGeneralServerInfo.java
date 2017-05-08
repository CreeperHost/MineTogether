package de.ellpeck.creeperhost.gui;

import cpw.mods.fml.client.config.GuiSlider;
import de.ellpeck.creeperhost.Util;
import de.ellpeck.creeperhost.common.AvailableResult;
import de.ellpeck.creeperhost.paul.Callbacks;
import de.ellpeck.creeperhost.paul.Constants;
import de.ellpeck.creeperhost.paul.Order;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiGeneralServerInfo extends GuiGetServer {

    private GuiTextField nameField;
    private GuiSlider slotSlider;

    private long lastKeyTyped;
    private String acceptString = new String(Character.toChars(10004));
    private String denyString = new String(Character.toChars(10006));
    private boolean isAcceptable = false;
    private boolean nameChecked = false;
    private String message = "Name can not be blank";

    public GuiGeneralServerInfo(int stepId, Order order){
        super(stepId, order);
    }

    @Override
    public void initGui(){
        super.initGui();

        int halfWidth = this.width/2;
        int halfHeight = this.height/2;

        this.nameField = new GuiTextField(this.fontRendererObj, halfWidth-100, halfHeight-30, 200, 20);
        this.nameField.setMaxStringLength(Constants.MAX_SERVER_NAME_LENGTH);
        this.nameField.setText(this.order.name);

        final Order orderTemp = this.order;

        this.slotSlider = new GuiSlider(0, halfWidth - 100, halfHeight + 20, 150, 20, Util.localize("slider.player_count") + ": ", "", Constants.MIN_PLAYER_COUNT, Constants.MAX_PLAYER_COUNT, this.order.playerAmount, false, true, new GuiSlider.ISlider()
        {
            @Override
            public void onChangeSliderValue(GuiSlider slider)
            {
                orderTemp.playerAmount = slider.getValueInt();
            }
        });
        this.slotSlider.width = 200;
        this.buttonList.add(this.slotSlider);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        this.nameField.updateCursorCounter();

        final String nameToCheck = this.nameField.getText().trim();
        boolean isEmpty = nameToCheck.isEmpty();

        if (lastKeyTyped + 400 < System.currentTimeMillis() && !nameChecked) {
            nameChecked = true;
            if (isEmpty)
            {
                message = "Name cannot be blank";
                isAcceptable = false;
            } else {
                Runnable task = new Runnable() {
                    @Override
                    public void run()
                    {
                        AvailableResult result = Callbacks.getNameAvailable(nameToCheck);
                        isAcceptable = result.getSuccess();
                        message = result.getMessage();
                    }
                };

                Thread thread = new Thread(task);
                thread.start();

                // Done in a thread as to not hold up the UI thread
            }
        }

        this.buttonNext.enabled = !isEmpty && nameChecked && isAcceptable;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode){
        if(!this.nameField.textboxKeyTyped(typedChar, keyCode)){
            super.keyTyped(typedChar, keyCode);
        }
        else{
            nameChecked = false;
            message = "Name not yet checked";
            this.order.name = this.nameField.getText().trim();
            lastKeyTyped = System.currentTimeMillis();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRendererObj, Util.localize("info.server_name"), this.width/2, this.height/2-45, -1);

        String renderedString;
        int colour;

        if (nameChecked && isAcceptable) {
            renderedString = this.acceptString;
            colour = 0x00FF00;
        } else {
            renderedString = this.denyString;
            colour = 0xFF0000;
        }

        GL11.glScaled(2.0F, 2.0F, 2.0F);
        this.drawString(this.fontRendererObj, renderedString, this.width / 4 + 53, this.height / 4 - 14, colour);
        GL11.glScaled(0.5F, 0.5F, 0.5F);

        this.nameField.drawTextBox();


        int xLeft = (this.width / 2) + 104;
        int xRight = xLeft + (this.fontRendererObj.getStringWidth(renderedString) * 2);
        int yTop = (this.height / 2) - 30;
        int yBottom = yTop + 13;

        if (mouseX >= xLeft && mouseX <= xRight && mouseY >= yTop && mouseY <= yBottom) {
            List list = new ArrayList();
            list.add(message);
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public String getStepName(){
        return Util.localize("gui.general_info");
    }

    /*
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
    */
}
