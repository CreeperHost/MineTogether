package net.creeperhost.minetogether.lib;

import net.minecraft.util.ResourceLocation;

public class Constants
{
    public static final String MOD_ID = "minetogether";
    public static final ResourceLocation CREEPER_HOST_BUTTON_LOCATION = new ResourceLocation(MOD_ID, "textures/gui.png");
    public static final ResourceLocation CREEPER_HOST_MENU_ICON = new ResourceLocation(MOD_ID, "textures/creeperhost.png");
    public static final ResourceLocation NO_BUTTON_ICON = new ResourceLocation(MOD_ID, "textures/nobrand.png");
    
    public static final int MAX_SERVER_NAME_LENGTH = 16;
    public static final int MIN_PLAYER_COUNT = 1;
    public static final int MAX_PLAYER_COUNT = 20;
    public static final int DEF_PLAYER_COUNT = 5;
    public static final String DEF_SERVER_LOCATION = "buffalo";
}
