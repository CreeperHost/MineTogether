package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.minecraft.client.gui.FontRenderer;

public class GuiTextFieldLockable extends GuiTextFieldCompat
{
    private boolean ourEnabled = true;
    private String disableText = "";
    private final FontRenderer fontRenderer;
    
    public GuiTextFieldLockable(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height)
    {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
        fontRenderer = fontrendererObj;
    }
    
    @Override
    public boolean getEnableBackgroundDrawing()
    {
        return false;
    }
    
    @Override
    public void drawTextBox()
    {
        super.drawTextBox();
        int colour = this.ourEnabled ? -6250336 : 0xFFFF0000;

        if (this.getEnableBackgroundDrawing()) {
            drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, colour);
            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
        }
        super.drawTextBox();
        
        int x = this.xPosition + 4;
        int y = this.yPosition + (this.height - 8) / 2;
        
        
        if (this.getText().trim().isEmpty() && !this.getOurEnabled())
        {
            fontRenderer.drawStringWithShadow("\u00A7o" + this.disableText, x, y, 14737632);
        }
        
        return;
    }
    
    @Override
    public void setEnabled(boolean enabled)
    {
        ourEnabled = enabled;
        super.setEnabled(enabled);
    }
    
    public void setDisabled(String message)
    {
        setEnabled(false);
        disableText = message;
    }
    
    public boolean isHovered(int mouseX, int mouseY)
    {
        return mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;
    }
    
    public boolean getOurEnabled()
    {
        return ourEnabled;
    }
    
    public String getDisabledMessage()
    {
        return disableText;
    }
}
