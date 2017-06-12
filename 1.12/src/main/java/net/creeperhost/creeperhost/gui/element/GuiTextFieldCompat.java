package net.creeperhost.creeperhost.gui.element;

import net.creeperhost.creeperhost.Util;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.lang.reflect.Method;

/**
 * Created by Aaron on 11/06/2017.
 */
public class GuiTextFieldCompat extends GuiTextField
{
    public GuiTextFieldCompat(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height)
    {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    private static Method mouseClickedMethod;

    // Hack to call the real one so we can support 1.9.4-1.10.2 and 1.11.2
    public boolean myMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseClickedMethod == null)
        {
            mouseClickedMethod = Util.findMethod(GuiTextField.class, new String[]{"func_146192_a", "mouseClicked"}, int.class, int.class, int.class );
        }

        try
        {
            Object result = mouseClickedMethod.invoke(this, mouseX, mouseY, mouseButton);
            return result == null ? true : Boolean.valueOf(result.toString());
        } catch (Throwable t) {
            return false;
        }
    }
}
