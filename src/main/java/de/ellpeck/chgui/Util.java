package de.ellpeck.chgui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public final class Util{

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation(CreeperHostGui.MOD_ID,"textures/gui.png");

    public static String localize(String key, Object... format){
        return I18n.format(CreeperHostGui.MOD_ID+"."+key, format);
    }

}
