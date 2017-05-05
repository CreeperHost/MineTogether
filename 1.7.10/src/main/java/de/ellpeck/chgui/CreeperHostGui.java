package de.ellpeck.chgui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ellpeck.chgui.common.Config;
import de.ellpeck.chgui.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

@Mod(modid = CreeperHostGui.MOD_ID, name = CreeperHostGui.NAME, version = CreeperHostGui.VERSION)
public class CreeperHostGui{

    public static final String MOD_ID = "chgui";
    public static final String NAME = "CreeperHost Gui";
    public static final String VERSION = "@VERSION@";
    public static final Logger logger = LogManager.getLogger("CreeperHostIGS");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        File configFile = event.getSuggestedConfigurationFile();
        if (!configFile.exists()) {
            BufferedWriter writer = null;
            InputStream defaultInputStream = null;
            try
            {
                writer = new BufferedWriter( new FileWriter( configFile ));
                ResourceLocation location = new ResourceLocation("chgui", "default.config");
                defaultInputStream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();

                String defaultConfigString = IOUtils.toString(defaultInputStream);

                writer.write(defaultConfigString);

            }
            catch (IOException e)
            {
                logger.error("Error occured whilst creating default config. This will not end well.", e);
            }
            finally
            {
                try
                {
                    if ( writer != null)
                        writer.close( );
                    if ( defaultInputStream != null )
                        defaultInputStream.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        InputStream configStream = null;
        try
        {
            configStream = new FileInputStream(configFile);
            String configString = IOUtils.toString(configStream);
            JsonObject jObject = new JsonParser().parse(configString).getAsJsonObject();
            Config.makeConfig(Callbacks.getVersionFromCurse(jObject.getAsJsonPrimitive("curseProjectID").getAsString()), jObject.getAsJsonPrimitive("promoCode").getAsString());
        } catch (Throwable t)
        {
            logger.error("Unable to read config", t);
            throw new RuntimeException("Fatal error");
        } finally {
            try {
                if (configStream != null) {
                    configStream.close();
                }
            } catch (Throwable t) {
            }

        }
    }
}
