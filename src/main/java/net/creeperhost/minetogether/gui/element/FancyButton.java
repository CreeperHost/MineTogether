package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FancyButton extends GuiButtonExt
{
    protected final FancyButton.IPressable onPress;

    public FancyButton(int id, int xPos, int yPos, String displayString, FancyButton.IPressable pressedAction)
    {
        super(id, xPos, yPos, displayString);
        this.onPress = pressedAction;
    }

    public FancyButton(int id, int xPos, int yPos, int width, int height, String displayString, FancyButton.IPressable pressedAction)
    {
        super(id, xPos, yPos, width, height, displayString);
        this.onPress = pressedAction;
    }

    public void onPress()
    {
        this.onPress.onPress(this);
    }

    @SideOnly(Side.CLIENT)
    public interface IPressable
    {
        void onPress(GuiButton button);
    }
}
