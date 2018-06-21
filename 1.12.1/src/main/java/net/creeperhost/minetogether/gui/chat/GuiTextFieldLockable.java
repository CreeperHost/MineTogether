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
    public void drawTextBox()
    {
        if (!this.ourEnabled && this.getText().trim().isEmpty())
        {
            String tempText = getText();
            int tempCursor = getCursorPosition();
            setText("");
            super.drawTextBox();
            setText(tempText);
            setCursorPosition(tempCursor);
            int x = this.xPosition + 4;
            int y = this.yPosition + (this.height - 8) / 2;

            fontRenderer.drawStringWithShadow("\u00A7o" + this.disableText, x, y, 14737632);
            return;
        }

        super.drawTextBox();
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
}
