package net.creeperhost.minetogether.util;

import com.google.common.hash.Hashing;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.covers1624.quack.util.HashUtils;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class SignatureVerifier {

    private static final Logger LOGGER = LogManager.getLogger();

    public static String generateSignature() {
        if (!Platform.isDevelopmentEnvironment() && System.getProperty("mt.develop.signature") == null) {
            Path modJar = MineTogetherPlatform.getModJar();
            if (modJar != null && modJar.toString().endsWith(".jar")) {
                try {
                    return HashUtils.hash(Hashing.sha256(), modJar).toString();
                } catch (IOException ex) {
                    LOGGER.error("Failed to hash mod jar.", ex);
                }
            }
        }
        return System.getProperty("mt.develop.signature", "Development");
    }
}
