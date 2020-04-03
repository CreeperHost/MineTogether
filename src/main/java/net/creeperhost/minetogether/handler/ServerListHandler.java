package net.creeperhost.minetogether.handler;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.server.MineTogetherPropertyManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

import java.io.File;

public class ServerListHandler
{
    boolean serverOn;

    public ServerListHandler()
    {
        if (!MineTogether.instance.active)
            return;
        final MinecraftServer server = MineTogether.server;
        if (server != null && !server.isSinglePlayer())
        {
            File properties = new File("server.properties");
            MineTogetherPropertyManager manager = new MineTogetherPropertyManager(properties);
            DedicatedServer dediServer = (DedicatedServer) server;

            String discoverModeString = manager.getStringProperty("discoverability", "unlisted");
            String displayNameTemp = manager.getStringProperty("displayname", "Fill this in if you have set the server to public!");
            String serverIP = manager.getStringProperty("server-ip", "");
            final String projectID = Config.getInstance().curseProjectID;

            if (displayNameTemp.equals("Fill this in if you have set the server to public!") && discoverModeString.equals("unlisted"))
            {
                File outProperties = new File("minetogether.properties");
                if (outProperties.exists())
                {
                    MineTogetherPropertyManager managerOut = new MineTogetherPropertyManager(outProperties);
                    displayNameTemp = managerOut.getStringProperty("displayname", "Fill this in if you have set the server to public!");
                    discoverModeString = managerOut.getStringProperty("discoverability", "unlisted");
                    serverIP = managerOut.getStringProperty("server-ip", "");
                } else
                {
                    displayNameTemp = "Unknown";
                    discoverModeString = "unlisted";
                }
            }

            final String displayName = displayNameTemp;

            serverOn = true;
            try
            {
                MineTogether.discoverMode = MineTogether.Discoverability.valueOf(discoverModeString.toUpperCase());
            } catch (IllegalArgumentException ignored)
            {
            }

            if (MineTogether.discoverMode != MineTogether.Discoverability.UNLISTED)
            {
                Config defConfig = new Config();
                if ((projectID.isEmpty() || projectID.equals(defConfig.curseProjectID)) && MineTogether.instance.base64 == null)
                {
                    MineTogether.logger.warn("Curse project ID minetogther.cfg not set correctly or version.json doesn't exist - please rectify this to utilize the server list feature.");
                    return;
                }
                MineTogether.startMinetogetherThread(serverIP, displayName, MineTogether.instance.base64 == null ? projectID : MineTogether.instance.base64, server.getServerPort(), MineTogether.discoverMode);
            }
        }
    }
}
