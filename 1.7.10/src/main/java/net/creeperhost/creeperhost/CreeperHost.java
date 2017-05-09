package net.creeperhost.creeperhost;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.relauncher.Side;
import net.creeperhost.creeperhost.api.IServerHost;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

@Mod(modid = CreeperHost.MOD_ID, name = CreeperHost.NAME, version = CreeperHost.VERSION, acceptableRemoteVersions="*")
public class CreeperHost
{

    public static final String MOD_ID = "creeperhost";
    public static final String NAME = "CreeperHost";
    public static final String VERSION = "@VERSION@";
    public static final Logger logger = LogManager.getLogger("creeperhost");

    @Mod.Instance
    public static CreeperHost instance;

    public ArrayList<IServerHost> implementations = new ArrayList<IServerHost>();
    public IServerHost currentImplementation;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        if (event.getSide() == Side.SERVER) {
            logger.info("Client side only mod - not doing anything on the server!");
            return;
        }
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        File configFile = event.getSuggestedConfigurationFile();
        if (!configFile.exists()) {
            BufferedWriter writer = null;
            InputStream defaultInputStream = null;
            try
            {
                writer = new BufferedWriter( new FileWriter( configFile ));
                ResourceLocation location = new ResourceLocation("creeperhost", "default.config");
                defaultInputStream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();

                String defaultConfigString = IOUtils.toString(defaultInputStream);

                writer.write(defaultConfigString);

            }
            catch (IOException e)
            {
                logger.error("Error occurred whilst creating default config. This will not end well.", e);
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
            Config.makeConfig(
                    Callbacks.getVersionFromCurse(jObject.getAsJsonPrimitive("curseProjectID").getAsString()),
                    jObject.getAsJsonPrimitive("promoCode").getAsString(),
                    jObject.getAsJsonPrimitive("creeperhostEnabled").getAsBoolean(),
                    jObject.getAsJsonPrimitive("mpMenuEnabled").getAsBoolean(),
                    jObject.getAsJsonPrimitive("mainMenuEnabled").getAsBoolean(),
                    jObject.getAsJsonPrimitive("serverHostButtonImage").getAsBoolean(),
                    jObject.getAsJsonPrimitive("serverHostMenuImage").getAsBoolean());
        } catch (Throwable t)
        {
            logger.error("Unable to read config", t);
            throw new RuntimeException("Fatal error, unable to read config");
        } finally {
            try {
                if (configStream != null) {
                    configStream.close();
                }
            } catch (Throwable t) {
            }

        }
    }

    private Random randomGenerator;

    public void setRandomImplementation() {
        if (randomGenerator == null)
            randomGenerator = new Random();
        if (implementations.size() == 0)
            return;
        int random = randomGenerator.nextInt(implementations.size());
        currentImplementation = implementations.get(random);
    }

    public IServerHost getImplementation()
    {
        return currentImplementation;
    }
}
