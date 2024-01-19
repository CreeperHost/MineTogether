package net.creeperhost.minetogether.util;

import com.mojang.authlib.GameProfile;
import dev.architectury.injectables.targets.ArchitecturyTarget;
import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.MineTogetherPlatform;
import net.creeperhost.minetogether.lib.MineTogetherLib;
import net.creeperhost.minetogether.session.MojangUtils;
import net.creeperhost.minetogether.session.SessionProvider;
import net.creeperhost.minetogether.session.data.mc.ProfileKeyPairResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by covers1624 on 24/8/23.
 */
public class MTSessionProvider implements SessionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTSessionProvider.class);
    private static final String UA =
            "MineTogether-lib/" + MineTogetherLib.VERSION +
            " MineTogether-mod/" + MineTogetherPlatform.getVersion() +
            " Minecraft/" + Platform.getMinecraftVersion() +
            " Modloader/" + ArchitecturyTarget.getCurrentTarget();
    private final Minecraft MC = Minecraft.getInstance();
    private final User U = MC.getUser();
    private final GameProfile P = MC.getUser().getGameProfile();

    // @formatter:off
    @Override public @Nullable UUID getUUID() { return P.getId(); }
    @Override public String getUsername() { return P.getName(); }
    @Override public @Nullable String beginAuth() throws IOException { return MojangUtils.joinServer(P.getId(), U.getAccessToken()); }
    @Override public @Nullable ProfileKeyPairResponse getProfileKeyPair() throws IOException { return MojangUtils.getProfileKeypair(U.getAccessToken()); }
    @Override public void infoLog(String msg, Object... args) { LOGGER.info(msg, args); }
    @Override public void warnLog(String msg, Object... args) { LOGGER.warn(msg, args); }
    @Override public void errorLog(String msg, Object... args) { LOGGER.error(msg, args); }
    @Override public String describe() { return UA; }
    // @formatter:on
}
