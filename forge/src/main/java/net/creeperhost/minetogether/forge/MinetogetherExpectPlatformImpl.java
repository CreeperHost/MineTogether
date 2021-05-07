package net.creeperhost.minetogether.forge;

import net.creeperhost.minetogether.MinetogetherExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class MinetogetherExpectPlatformImpl {
    /**
     * This is our actual method to {@link MinetogetherExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
