package de.ellpeck.chgui;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = CreeperHostGui.MOD_ID, name = CreeperHostGui.NAME, version = CreeperHostGui.VERSION)
public class CreeperHostGui{

    public static final String MOD_ID = "chgui";
    public static final String NAME = "CreeperHost Gui";
    public static final String VERSION = "@VERSION@";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){

    }
}
