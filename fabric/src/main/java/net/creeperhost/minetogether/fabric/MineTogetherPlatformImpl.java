package net.creeperhost.minetogether.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static net.creeperhost.minetogether.MineTogether.MOD_ID;

/**
 * Created by covers1624 on 26/8/22.
 */
public class MineTogetherPlatformImpl {

    @Nullable
    public static Path getModJar() {
        Optional<ModContainer> container = FabricLoader.getInstance()
                .getModContainer(MOD_ID);
        if (container.isEmpty()) return null;

        ModOrigin origin = container.get().getOrigin();
        if (origin.getKind() != ModOrigin.Kind.PATH) {
            return null;
        }
        List<Path> paths = origin.getPaths();
        return !paths.isEmpty() ? paths.get(0) : null;
    }
}
