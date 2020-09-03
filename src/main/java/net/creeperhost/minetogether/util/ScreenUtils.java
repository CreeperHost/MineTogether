package net.creeperhost.minetogether.util;

import net.minecraft.client.Minecraft;

import net.minecraft.util.text.TextFormatting;

public class ScreenUtils
{
    public static String removeTextColorsIfConfigured(String text, boolean forceColor)
    {
        return !forceColor && !Minecraft.getInstance().gameSettings.chatColor ? TextFormatting.getTextWithoutFormattingCodes(text) : text;
    }
}
