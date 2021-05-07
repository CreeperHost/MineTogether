package net.creeperhost.minetogether.fabric;

import net.creeperhost.minetogether.MinetogetherExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class MinetogetherExpectPlatformImpl {
    /**
     * This is our actual method to {@link MinetogetherExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
