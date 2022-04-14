package net.creeperhost.minetogether;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.EnvExecutor;
import io.sentry.Sentry;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.MineTogetherChat;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.nio.file.Path;

public class MineTogetherCommon
{
    public static Logger logger = LogUtils.getLogger();
    public static String base64 = "";
    public static Path configFile = Platform.getConfigFolder().resolve(Constants.MOD_ID + ".json");

    public static void init()
    {
        Sentry.init(options -> {
            options.setDsn("https://07fc3e3411eb4c44849d2eb1faa28092@sentry.creeperhost.net/7");
            options.setTracesSampleRate(Platform.isDevelopmentEnvironment() ? 1.0 : 0.025);
//            options.setEnvironment(SharedConstants.getCurrentVersion().getName());
            options.setRelease(Constants.VERSION);
//            options.setTag("commit", BuildInfo.version);
            options.setTag("modloader", Minecraft.getInstance().getLaunchedVersion());
            options.setTag("ram", String.valueOf(((Runtime.getRuntime().maxMemory() / 1024) /1024)));
            options.setDist(System.getProperty("os.arch"));
            options.setServerName(Platform.getEnv() == EnvType.CLIENT ? "integrated" : "dedicated");
            options.setDebug(Platform.isDevelopmentEnvironment());
        });
        try
        {
            Config.init(configFile.toFile());
            MineTogetherChat.DEBUG_MODE = Config.getInstance().isDebugMode();
        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
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
