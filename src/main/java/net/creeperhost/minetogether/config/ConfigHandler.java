package net.creeperhost.minetogether.config;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.lib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;

import java.io.File;
import java.io.FileWriter;

public class ConfigHandler
{
    public static void init(Dist dist)
    {
        File base;

        if(dist == Dist.DEDICATED_SERVER)
        {
            MinecraftServer dedicatedServer = MineTogether.server;
            base = dedicatedServer.getDataDirectory();
        }
        else
        {
            base = Minecraft.getInstance().gameDir;
        }

        File configDir = new File(base + File.separator + "config");
        if (configDir.exists())
        {
            try
            {
                File f1 = new File(configDir + File.separator + Constants.MOD_ID + ".json");
                if (!f1.exists())
                {
                    Config.instance = new Config();

                    FileWriter tileWriter = new FileWriter(configDir + "/" + Constants.MOD_ID + ".json");
                    tileWriter.write(Config.saveConfig());
                    tileWriter.close();
                } else
                {
                    Config.loadFromFile(f1);
                }
            } catch (Exception ignored) {}
        }
    }
}
