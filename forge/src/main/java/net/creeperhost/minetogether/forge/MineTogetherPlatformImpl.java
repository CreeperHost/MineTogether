package net.creeperhost.minetogether.forge;

import net.minecraft.network.Connection;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import static net.creeperhost.minetogether.MineTogether.MOD_ID;

/**
 * Created by covers1624 on 26/8/22.
 */
public class MineTogetherPlatformImpl {

    @Nullable
    public static Path getModJar() {
        ModFileInfo fileInfo = ModList.get().getModFileById(MOD_ID);
        if (fileInfo == null) {
            return null;
        }
        return fileInfo.getFile().getFilePath();
    }

    public static String getVersion() {
        ModFileInfo fileInfo = ModList.get().getModFileById(MOD_ID);
        if (fileInfo == null) {
            return "UNKNOWN";
        }

        return fileInfo.getMods().get(0).getVersion().toString();
    }

    public static void prepareClientConnection(Connection connection) {
        NetworkHooks.registerClientLoginChannel(connection);
    }
}
