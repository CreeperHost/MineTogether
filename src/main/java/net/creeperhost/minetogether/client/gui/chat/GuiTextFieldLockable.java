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
