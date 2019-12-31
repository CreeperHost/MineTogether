package net.creeperhost.minetogether.config;

import net.creeperhost.minetogether.lib.ModInfo;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;

public class ConfigHandler
{
    public static void init()
    {
        //TODO this will not work on servers...
        File base = Minecraft.getInstance().gameDir;
        
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
