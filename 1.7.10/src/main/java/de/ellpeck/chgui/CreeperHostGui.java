package de.ellpeck.chgui;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = CreeperHostGui.MOD_ID, name = CreeperHostGui.NAME, version = CreeperHostGui.VERSION)
public class CreeperHostGui{

    public static final String MOD_ID = "chgui";
    public static final String NAME = "CreeperHost Gui";
    public static final String VERSION = "@VERSION@";
    public static final Logger logger = LogManager.getLogger("CreeperHostIGS");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
}
