package net.creeperhost.minetogether;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Created by covers1624 on 26/8/22.
 */
public class MineTogetherPlatform {

    @Nullable
    @ExpectPlatform
    public static Path getModJar() {
        throw new AssertionError();
    }
}
