package net.creeperhost.minetogether.util;

import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.session.MojangUtils;
import net.creeperhost.minetogether.session.SessionProvider;
import net.creeperhost.minetogether.session.data.mc.ProfileKeyPairResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.common.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by covers1624 on 24/8/23.
 */
public class MTSessionProvider implements SessionProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String UA =
            "MineTogether-mod/" + CreeperHost.VERSION +
            " Minecraft" + ForgeVersion.mcVersion +
            " Modloader/Forge";

    private final Minecraft MC = Minecraft.getMinecraft();
    private final Session S = MC.getSession();
    private final GameProfile P = S.getProfile();

    // @formatter:off
    @Override public @Nullable UUID getUUID() { return P.getId(); }
    @Override public String getUsername() { return P.getName(); }
    @Override public @Nullable String beginAuth() throws IOException { return MojangUtils.joinServer(P.getId(), S.getToken()); }
    @Override public @Nullable ProfileKeyPairResponse getProfileKeyPair() throws IOException { return MojangUtils.getProfileKeypair(S.getToken()); }
    @Override public void infoLog(String msg, Object... args) { LOGGER.info(msg, args); }
    @Override public void warnLog(String msg, Object... args) { LOGGER.warn(msg, args); }
    @Override public void errorLog(String msg, Object... args) { LOGGER.error(msg, args); }
    @Override public String describe() { return UA; }
    // @formatter:on
}
