package net.creeperhost.minetogether.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class MinetogetherExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static String getModLoader()
    {
        return "Fabric";
    }
}
