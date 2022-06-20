package net.creeperhost.minetogether;

import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main common mod entrypoint.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class MineTogether {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "minetogether";

    public static void init() {
        LOGGER.info("Initializing MineTogether!");
        Config.loadConfig(Platform.getConfigFolder().resolve(MOD_ID + ".json"));
        if (Config.instance().debugMode) {
            LOGGER.warn("Debug mode enabled. Prepare for _VERY_ verbose logging!");
        }

        switch (Platform.getEnv()) {
            case CLIENT -> MineTogetherClient.init();
            case SERVER -> MineTogetherServer.init();
        }
    }
}
