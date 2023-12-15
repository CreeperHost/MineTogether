package net.creeperhost.minetogether.neoforge;

import net.minecraft.network.Connection;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.NetworkHooks;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import static net.creeperhost.minetogether.MineTogether.MOD_ID;

/**
 * Created by covers1624 on 26/8/22.
 */
public class MineTogetherPlatformImpl {

    @Nullable
    public static Path getModJar() {
        IModFileInfo fileInfo = ModList.get().getModFileById(MOD_ID);
        if (fileInfo == null) {
            return null;
        }
        return fileInfo.getFile().getFilePath();
    }

    public static String getVersion() {
        IModFileInfo fileInfo = ModList.get().getModFileById(MOD_ID);
        if (fileInfo == null) {
            return "UNKNOWN";
        }

        return fileInfo.versionString();
    }

    public static void prepareClientConnection(Connection connection) {
        NetworkHooks.registerClientLoginChannel(connection);
    }
}