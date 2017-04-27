package net.creeperhost.ingamesale;

import net.creeperhost.ingamesale.common.IGSCommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = IngameSale.MODID, version = IngameSale.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "1.7.10,1.8.9,1.9.4,1.10.2,1.11.2")
public class IngameSale
{
    public static final String MODID = "creeperhostigs";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide="net.creeperhost.ingamesale.client.IGSClientProxy", serverSide="net.creeperhost.ingamesale.common.IGSCommonProxy")
    private static IGSCommonProxy proxy;

    public static final Logger logger = LogManager.getLogger("CreeperHostIGS");
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.doClientStuff();
    }
}
