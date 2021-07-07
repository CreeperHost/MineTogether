package net.creeperhost.minetogether;

import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.creeperhost.minetogether.config.Config;
import net.fabricmc.api.EnvType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class MineTogether
{
    public static Logger logger = LogManager.getLogger();
    public static String base64 = "";
    public static Path configFile = Platform.getConfigFolder().resolve(Constants.MOD_ID + ".json");

    public static void init()
    {
        Config.init(configFile.toFile());
    }

    public static void clientInit()
    {
        EnvExecutor.runInEnv(EnvType.CLIENT, () -> MineTogetherClient::init);
    }

    public static void serverInit()
    {
        EnvExecutor.runInEnv(EnvType.SERVER, () -> MineTogetherServer::init);
    }
}
