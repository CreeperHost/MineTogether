package net.creeperhost.minetogether.chat;

import com.google.common.hash.Hashing;
import com.mojang.authlib.exceptions.AuthenticationException;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.lib.chat.ChatAuth;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author covers1624
 */
@SuppressWarnings ("UnstableApiUsage")
public class ChatAuthImpl implements ChatAuth {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Minecraft mc;
    private final UUID uuid;
    private final String uuidHash;
    private final boolean isOnline;

    public ChatAuthImpl(Minecraft mc) {
        this.mc = mc;
        uuid = UUIDUtil.getOrCreatePlayerUUID(mc.getUser().getGameProfile());
        uuidHash = Hashing.sha256().hashString(uuid.toString(), UTF_8).toString().toUpperCase(Locale.ROOT);
        isOnline = uuid.version() == 4; // Version 4 UUID's are online (fully random), Version3 UUID's are offline (generated from md5 string hash).
    }

    @Override
    public String getSignature() {
        return MineTogether.FINGERPRINT;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getHash() {
        return uuidHash;
    }

    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public String beginMojangAuth() {
        String serverId = Hashing.sha1().hashString(UUID.randomUUID().toString(), UTF_8).toString();
        try {
            mc.getMinecraftSessionService().joinServer(mc.getUser().getGameProfile(), mc.getUser().getAccessToken(), serverId);
            return serverId;
        } catch (AuthenticationException ex) {
            LOGGER.error("Failed to send 'joinServer' request.", ex);
        }
        return null;
    }
}
