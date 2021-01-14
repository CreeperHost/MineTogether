package net.creeperhost.minetogether.client.screen.order;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.AvailableResult;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.element.GuiTextFieldValidate;
import net.creeperhost.minetogether.lib.Constants;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class GuiGeneralServerInfo extends GuiGetServer
{
    private static ResourceLocation lockIcon;
    private TextFieldWidget nameField;
    private Slider slotSlider;
    private long lastKeyTyped;
    private boolean isAcceptable = false;
    private boolean nameChecked = false;
    private String message = "Name can not be blank";
    private CheckboxButton pregen;
    private Button modpack;
    
    public GuiGeneralServerInfo(int stepId, Order order)
    {
        super(stepId, order);
        lockIcon = new ResourceLocation(Constants.MOD_ID, "textures/lock.png");
    }
    
    @Override
    public void init()
    {
        super.init();
        
        int halfWidth = this.width / 2;
        int halfHeight = this.height / 2;
        
        this.nameField = new GuiTextFieldValidate(this.font, halfWidth - 100, halfHeight - 50, 200, 20, "([A-Za-z0-9]*)", "");
        this.nameField.setMaxStringLength(Constants.MAX_SERVER_NAME_LENGTH);
        this.nameField.setText(this.order.name.isEmpty() ? Util.getDefaultName() : this.order.name);
        this.order.name = this.nameField.getText().trim();
        
        String checkboxString = Util.localize("info.pregen");
        
        int checkboxWidth = this.font.getStringWidth(checkboxString) + 11 + 2;
        
        pregen = addButton(new CheckboxButton(halfWidth - (checkboxWidth / 2), halfHeight - 8, 20, 20, new StringTextComponent(checkboxString), order.pregen));

        modpack = addButton(new Button(4, 4, 86, 20, new StringTextComponent("Change Modpack"), b -> {
            Minecraft.getInstance().displayGuiScreen(new GuiModPackList(this));
        }));
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void tick()
    {
        super.tick();
        this.nameField.tick();
        
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
                Runnable task = () ->
                {
                    AvailableResult result = Callbacks.getNameAvailable(nameToCheck);
                    isAcceptable = result.getSuccess();
                    message = result.getMessage();
                };
                
                Thread thread = new Thread(task);
                thread.start();
            }
        }
        this.buttonNext.active = !isEmpty && nameChecked && isAcceptable;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean charTyped(char typedChar, int keyCode)
    {
        String nameFieldOldValue = nameField.getText();
        if (!this.nameField.charTyped(typedChar, keyCode))
        {
            super.charTyped(typedChar, keyCode);
        } else
        {
            if (!nameFieldOldValue.equals(nameField.getText()))
            {
                nameChecked = false;
                message = "Name not yet checked";
                this.order.name = this.nameField.getText().trim();
                lastKeyTyped = System.currentTimeMillis();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        fill(matrixStack, 0, this.height - 20, width, 20, 0x99000000);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        
        this.drawCenteredString(matrixStack, this.font, Util.localize("info.server_name"), this.width / 2, this.height / 2 - 65, -1);
        
        int colour;
        
        if (nameChecked && isAcceptable)
        {
            colour = 0x00FF00;
        } else
        {
            colour = 0xFF0000;
        }
        
        this.minecraft.getTextureManager().bindTexture(lockIcon);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(matrixStack, (this.width / 2) - 8, (this.height / 2) + 40, 0.0F, 0.0F, 16, 16, 16, 16);
        
        int strStart = 61;
        
        this.drawCenteredString(matrixStack, font, Util.localize("secure.line1"), this.width / 2, (this.height / 2) + strStart, 0xFFFFFF);
        this.drawCenteredString(matrixStack, font, Util.localize("secure.line2"), this.width / 2, (this.height / 2) + strStart + 10, 0xFFFFFF);
        this.drawCenteredString(matrixStack, font, Util.localize("secure.line3"), this.width / 2, (this.height / 2) + strStart + 20, 0xFFFFFF);
        
        this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
        
        this.drawCenteredString(matrixStack, font, message, (this.width / 2), (this.height / 2) - 26, colour);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        order.pregen = pregen.isChecked();
        return true;
    }
    
    @Override
    public String getStepName()
    {
        return Util.localize("gui.general_info");
    }
}
