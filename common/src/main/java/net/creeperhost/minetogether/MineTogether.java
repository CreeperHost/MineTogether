package net.creeperhost.minetogether;

import dev.architectury.injectables.targets.ArchitecturyTarget;
import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.MineTogetherLib;
import net.creeperhost.minetogether.util.Log4jUtils;
import net.creeperhost.minetogether.lib.web.ApiClient;
import net.creeperhost.minetogether.lib.web.DynamicWebAuth;
import net.creeperhost.minetogether.lib.web.WebEngine;
import net.creeperhost.minetogether.lib.web.apache.ApacheWebEngine;
import net.creeperhost.minetogether.orderform.WebUtils;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.creeperhost.minetogether.util.SignatureVerifier;
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

    public static final String FINGERPRINT = SignatureVerifier.generateSignature();
    public static final DynamicWebAuth AUTH = new DynamicWebAuth();
    public static final WebEngine WEB_ENGINE = new ApacheWebEngine();
    public static final ApiClient API = ApiClient.builder()
            .webEngine(WEB_ENGINE)
            .addUserAgentSegment("MineTogether-lib/" + MineTogetherLib.VERSION)
            .addUserAgentSegment("MineTogether-mod/" + MineTogetherPlatform.getVersion())
            .addUserAgentSegment("Minecraft/" + Platform.getMinecraftVersion())
            .addUserAgentSegment("Modloader/" + ArchitecturyTarget.getCurrentTarget())
            .webAuth(AUTH)
            .build();
    static {
        WebUtils.userAgent += " MineTogether-lib/" + MineTogetherLib.VERSION;
        WebUtils.userAgent += " MineTogether-mod/" + MineTogetherPlatform.getVersion();
        WebUtils.userAgent += " Minecraft/" + Platform.getMinecraftVersion();
        WebUtils.userAgent += " Modloader/" + ArchitecturyTarget.getCurrentTarget();
    }

    public static void init() {
        Log4jUtils.attachMTLogs(Platform.getGameFolder().resolve("logs"));
        LOGGER.info("Initializing MineTogether!");
        AUTH.setHeader("Fingerprint", FINGERPRINT);

        Config.loadConfig(Platform.getConfigFolder().resolve(MOD_ID + ".json"));
        if (Config.instance().debugMode) {
            LOGGER.warn("Debug mode enabled. Prepare for _VERY_ verbose logging!");
        }

        ModPackInfo.init();
        ModPackInfo.waitForInfo(info -> AUTH.setHeader("Identifier", info.realName));
        switch (Platform.getEnv()) {
            case CLIENT -> MineTogetherClient.init();
            case SERVER -> MineTogetherServer.init();
        }
    }
}
