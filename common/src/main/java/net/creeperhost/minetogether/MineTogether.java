package net.creeperhost.minetogether;

import dev.architectury.injectables.targets.ArchitecturyTarget;
import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.web.ApiClient;
import net.creeperhost.minetogether.lib.web.DynamicWebAuth;
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
    public static final ApiClient API = ApiClient.builder()
            .webEngine(new ApacheWebEngine())
            .addUserAgentSegment("MineTogether-lib/@VERSION@")
            .addUserAgentSegment("MineTogether-mod/@VERSION@")
            .addUserAgentSegment("Minecraft/" + Platform.getMinecraftVersion())
            .addUserAgentSegment("Modloader/" + ArchitecturyTarget.getCurrentTarget())
            .webAuth(AUTH)
            .build();
    static {
        WebUtils.userAgent += " MineTogether-lib/@VERSION@";
        WebUtils.userAgent += " MineTogether-mod/@VERSION@";
        WebUtils.userAgent += " Minecraft/" + Platform.getMinecraftVersion();
        WebUtils.userAgent += " Modloader/" + ArchitecturyTarget.getCurrentTarget();
    }

    public static void init() {
        LOGGER.info("Initializing MineTogether!");
        AUTH.setHeader("Fingerprint", FINGERPRINT);

        Config.loadConfig(Platform.getConfigFolder().resolve(MOD_ID + ".json"));
        if (Config.instance().debugMode) {
            LOGGER.warn("Debug mode enabled. Prepare for _VERY_ verbose logging!");
        }

        ModPackInfo.init();
        switch (Platform.getEnv()) {
            case CLIENT -> MineTogetherClient.init();
            case SERVER -> MineTogetherServer.init();
        }
    }
}
