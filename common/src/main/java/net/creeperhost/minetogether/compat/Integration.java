package net.creeperhost.minetogether.compat;

import dev.architectury.platform.Platform;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 14/07/2024
 */
public class Integration {

    public static void loadOptionalIntegration(String modid, Supplier<Runnable> runnable) {
        if (Platform.isModLoaded(modid)) {
            runnable.get().run();
        }
    }

}
