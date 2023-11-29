package net.creeperhost.minetogether.util;

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
    private final Minecraft MC = Minecraft.getInstance();
    private final User U = MC.getUser();
    private final String PN = U.getName();
    private final UUID PI = U.getProfileId();

    // @formatter:off
    @Override public @Nullable UUID getUUID() { return PI; }
    @Override public String getUsername() { return PN; }
    @Override public @Nullable String beginAuth() throws IOException { return MojangUtils.joinServer(PI, U.getAccessToken()); }
    @Override public @Nullable ProfileKeyPairResponse getProfileKeyPair() throws IOException { return MojangUtils.getProfileKeypair(U.getAccessToken()); }
    @Override public void infoLog(String msg, Object... args) { LOGGER.info(msg, args); }
    @Override public void warnLog(String msg, Object... args) { LOGGER.warn(msg, args); }
    @Override public void errorLog(String msg, Object... args) { LOGGER.error(msg, args); }
    // @formatter:on
}
