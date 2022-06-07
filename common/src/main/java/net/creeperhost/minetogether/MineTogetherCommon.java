package net.creeperhost.minetogether;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import io.sentry.Sentry;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.ChatConnectionStatus;
import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.minetogether.lib.chat.MineTogetherChat;
import net.fabricmc.api.EnvType;
import org.slf4j.Logger;

import java.nio.file.Path;

public class MineTogetherCommon
{
    public static Logger logger = LogUtils.getLogger();
    public static String base64 = "";
    public static Path configFile = Platform.getConfigFolder().resolve(Constants.MOD_ID + ".json");

    public static void init()
    {
        Config.init(configFile.toFile());

        if(!Config.getInstance().isOptOutSentry())
        {
            Sentry.init(options ->
            {
                options.setDsn("https://07fc3e3411eb4c44849d2eb1faa28092@sentry.creeperhost.net/7");
                options.setTracesSampleRate(Platform.isDevelopmentEnvironment() ? 1.0 : 0.025);
                options.setEnvironment(Platform.getMinecraftVersion());
                options.setRelease(Constants.VERSION);
                //            options.setTag("commit", BuildInfo.version);
                options.setTag("modloader", Platform.isForge() ? "forge" : "fabric");
                options.setTag("ram", String.valueOf(((Runtime.getRuntime().maxMemory() / 1024) / 1024)));
                options.setDist(System.getProperty("os.arch"));
                options.setServerName(Platform.getEnv() == EnvType.CLIENT ? "integrated" : "dedicated");
                options.setDebug(Platform.isDevelopmentEnvironment());
                options.addInAppInclude("net.creeperhost.minetogether");
            });
        }
        try
        {
            MineTogetherChat.DEBUG_MODE = Config.getInstance().isDebugMode();

            if(Platform.getEnv() == EnvType.CLIENT) MineTogetherClient.init();
            if(Platform.getEnv() == EnvType.SERVER) MineTogetherServer.init();

        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
    }

    public static void sentryException(Throwable throwable)
    {
        Sentry.setTag("verified", ChatHandler.connectionStatus == ChatConnectionStatus.VERIFIED ? "true" : "false");
        Sentry.setTag("banned", ChatHandler.connectionStatus == ChatConnectionStatus.BANNED ? "true" : "false");
        Sentry.captureException(throwable);
    }
}
