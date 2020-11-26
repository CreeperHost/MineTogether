package net.creeperhost.minetogether.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.*;

import java.util.List;

public class ScreenUtils
{
    public static String removeTextColorsIfConfigured(String text, boolean forceColor)
    {
        return !forceColor && !Minecraft.getInstance().gameSettings.chatColor ? TextFormatting.getTextWithoutFormattingCodes(text) : text;
    }

    public static Widget findButton(String buttonString, List<Widget> widgetList)
    {
        for(Widget widget : widgetList)
        {
            if(widget.getMessage().getString().equalsIgnoreCase(I18n.format(buttonString)))
            {
                return widget;
            }
        }
        return null;
    }

    public static Widget removeButton(String buttonString, List<Widget> widgetList)
    {
        Widget widget = findButton(buttonString, widgetList);
        if(widget != null)
        {
            widget.visible = false;
            widget.active = false;
            return widget;
        }
        return null;
    }
}
