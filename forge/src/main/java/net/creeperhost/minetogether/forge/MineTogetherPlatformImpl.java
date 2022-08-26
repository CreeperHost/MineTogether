package net.creeperhost.minetogether.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;

import java.nio.file.Path;

import static net.creeperhost.minetogether.MineTogether.MOD_ID;

/**
 * Created by covers1624 on 26/8/22.
 */
public class MineTogetherPlatformImpl {

    public static Path getModJar() {
        IModFileInfo fileInfo = ModList.get().getModFileById(MOD_ID);
        if (fileInfo == null) {
            throw new IllegalStateException("Failed to find ModFileInfo for MineTogether?");
        }
        return fileInfo.getFile().getFilePath();
    }
}
