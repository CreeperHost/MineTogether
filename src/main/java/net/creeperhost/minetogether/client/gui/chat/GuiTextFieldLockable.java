package net.creeperhost.minetogether.client.gui.chat;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiTextFieldLockable extends TextFieldWidget
{
    private boolean ourEnabled = true;
    private String disableText = "";
    private final FontRenderer fontRenderer;
    
    public GuiTextFieldLockable(FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, String s)
    {
        super(fontrendererObj, x, y, par5Width, par6Height, s);
        fontRenderer = fontrendererObj;
    }

//    @Override
//    public void drawTextBox()
//    {
//        super.drawTextBox();
//        int colour = this.ourEnabled ? -6250336 : 0xFFFF0000;
//
//        if (this.getEnableBackgroundDrawing()) {
//            drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, colour);
//            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
//        }
//        super.drawTextBox();
//
//        int x = this.xPosition + 4;
//        int y = this.yPosition + (this.height - 8) / 2;
//
//
//        if (this.getText().trim().isEmpty() && !this.getOurEnabled())
//        {
//            fontRenderer.drawStringWithShadow("\u00A7o" + this.disableText, x, y, 14737632);
//        }
//    }
    
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
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
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
