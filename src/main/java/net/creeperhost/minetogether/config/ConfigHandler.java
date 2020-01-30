package net.creeperhost.minetogether.config;

import net.creeperhost.minetogether.lib.ModInfo;

import java.io.File;
import java.io.FileWriter;

public class ConfigHandler
{
    public static void init()
    {
        File base = new File("");
        
        File configDir = new File(base + File.separator + "config");
        if (configDir.exists())
        {
            try
            {
                File f1 = new File(configDir + File.separator + ModInfo.MOD_ID + ".json");
                if (!f1.exists())
                {
                    Config.instance = new Config();
                    
                    FileWriter tileWriter = new FileWriter(configDir + "/" + ModInfo.MOD_ID + ".json");
                    tileWriter.write(Config.saveConfig());
                    tileWriter.close();
                } else
                {
                    Config.loadFromFile(f1);
                }
            } catch (Exception ignored)
            {
            }
        }
    }
}
