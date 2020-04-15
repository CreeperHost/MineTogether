package net.creeperhost.minetogether.config;

import net.creeperhost.minetogether.lib.Constants;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;

public class ConfigHandler
{
    public static File CONFIG_LOCATION = new File(FMLPaths.CONFIGDIR.get().toFile() + File.separator + Constants.MOD_ID + ".json");
    
    public static void init()
    {
        try
        {
            if (!CONFIG_LOCATION.exists())
            {
                Config.instance = new Config();
                
                FileWriter tileWriter = new FileWriter(CONFIG_LOCATION);
                tileWriter.write(Config.saveConfig());
                tileWriter.close();
            } else
            {
                Config.loadFromFile(CONFIG_LOCATION);
            }
        } catch (Exception ignored)
        {
        }
    }
    
    public static void saveConfig()
    {
        try (FileOutputStream configOut = new FileOutputStream(CONFIG_LOCATION))
        {
            IOUtils.write(Config.saveConfig(), configOut, Charset.defaultCharset());
        } catch (Throwable ignored)
        {
        }
    }
}
