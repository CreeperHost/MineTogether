package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.AvailableResult;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.element.GuiTextFieldValidate;
import net.creeperhost.minetogether.misc.Callbacks;
import net.creeperhost.minetogether.misc.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;

import java.io.IOException;

public class GuiGeneralServerInfo extends GuiGetServer implements GuiPageButtonList.GuiResponder
{
    private static final GuiSlider.FormatHelper SLIDER_FORMATTER = (id, name, value) -> name + ": " + (int) value;
    private static ResourceLocation lockIcon;
    private GuiTextFieldCompat nameField;
    private GuiSlider slotSlider;
    private long lastKeyTyped;
    private String acceptString = new String(Character.toChars(10004));
    private String denyString = new String(Character.toChars(10006));
    private boolean isAcceptable = false;
    private boolean nameChecked = false;
    private String message = "Name can not be blank";
    private GuiCheckBox pregen;
    private GuiButton modpack;
    
    public GuiGeneralServerInfo(int stepId, Order order)
    {
        super(stepId, order);
        lockIcon = new ResourceLocation(CreeperHost.MOD_ID, "textures/lock.png");
    }
    
    @Override
    public void initGui()
    {
        super.initGui();

        int halfWidth = this.width / 2;
        int halfHeight = this.height / 2;
        
        this.nameField = new GuiTextFieldValidate(0, this.fontRendererObj, halfWidth - 100, halfHeight - 50, 200, 20, "([A-Za-z0-9]*)");
        this.nameField.setMaxStringLength(Constants.MAX_SERVER_NAME_LENGTH);
        this.nameField.setText(this.order.name.isEmpty() ? Util.getDefaultName() : this.order.name);
        this.order.name = this.nameField.getText().trim();
        
        String checkboxString = Util.localize("info.pregen");
        
        int checkboxWidth = this.fontRendererObj.getStringWidth(checkboxString) + 11 + 2;
        
        pregen = new GuiCheckBox(3, halfWidth - (checkboxWidth / 2), halfHeight - 8, checkboxString, order.pregen);
        
        if (Config.getInstance().getPregenDiameter() > 0)
        {
            this.buttonList.add(pregen);
        }

        this.slotSlider = new GuiSlider(this, 1, halfWidth - 100, halfHeight + 15, Util.localize("slider.player_count"), Constants.MIN_PLAYER_COUNT, Constants.MAX_PLAYER_COUNT, this.order.playerAmount, SLIDER_FORMATTER);
        this.slotSlider.width = 200;
        this.buttonList.add(this.slotSlider);
        modpack = new GuiButton(21212, 4, 4, 86, 20, "Change Modpack");
        buttonList.add(modpack);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        this.nameField.updateCursorCounter();
        
        final String nameToCheck = this.nameField.getText().trim();
        boolean isEmpty = nameToCheck.isEmpty();
        
        if (lastKeyTyped + 400 < System.currentTimeMillis() && !nameChecked)
        {
            nameChecked = true;
            if (isEmpty)
            {
                message = "Name cannot be blank";
                isAcceptable = false;
            } else
            {
                Runnable task = () -> {
                    AvailableResult result = Callbacks.getNameAvailable(nameToCheck);
                    isAcceptable = result.getSuccess();
                    message = result.getMessage();
                };
                
                Thread thread = new Thread(task);
                thread.start();
                // Done in a thread as to not hold up the UI thread
            }
        }
        this.buttonNext.enabled = !isEmpty && nameChecked && isAcceptable;
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        String nameFieldOldValue = nameField.getText();
        if (!this.nameField.textboxKeyTyped(typedChar, keyCode))
        {
            super.keyTyped(typedChar, keyCode);
        }
        else
        {
            if (!nameFieldOldValue.equals(nameField.getText()))
            {
                nameChecked = false;
                message = "Name not yet checked";
                this.order.name = this.nameField.getText().trim();
                lastKeyTyped = System.currentTimeMillis();
            }
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRendererObj, Util.localize("info.server_name"), this.width / 2, this.height / 2 - 65, -1);
        
        int colour;
        
        if (nameChecked && isAcceptable)
        {
            colour = 0x00FF00;
        } else
        {
            colour = 0xFF0000;
        }
        
        this.mc.getTextureManager().bindTexture(lockIcon);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture((this.width / 2) - 8, (this.height / 2) + 40, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
        
        int strStart = 61;
        
        this.drawCenteredString(fontRendererObj, Util.localize("secure.line1"), this.width / 2, (this.height / 2) + strStart, 0xFFFFFF);
        this.drawCenteredString(fontRendererObj, Util.localize("secure.line2"), this.width / 2, (this.height / 2) + strStart + 10, 0xFFFFFF);
        this.drawCenteredString(fontRendererObj, Util.localize("secure.line3"), this.width / 2, (this.height / 2) + strStart + 20, 0xFFFFFF);
        
        this.nameField.drawTextBox();
        
        this.drawCenteredString(fontRendererObj, message, (this.width / 2), (this.height / 2) - 26, colour);

        drawGradientRect(50, this.height - 20, width, 20, 0x66000000, 0x66000000);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.myMouseClicked(mouseX, mouseY, mouseButton);
        order.pregen = pregen.isChecked();
    }
    
    @Override
    public String getStepName()
    {
        return Util.localize("gui.general_info");
    }
    
    @Override
    public void setEntryValue(int id, boolean value) {}
    
    @Override
    public void setEntryValue(int id, float value)
    {
        this.order.playerAmount = (int) value;
    }
    
    @Override
    public void setEntryValue(int id, String value) {}

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        if(button == modpack)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiModPackList(this));
        }
    }
}
