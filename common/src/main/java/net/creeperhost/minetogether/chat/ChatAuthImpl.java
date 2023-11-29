package net.creeperhost.minetogether.chat;

import com.google.common.hash.Hashing;
import com.mojang.authlib.exceptions.AuthenticationException;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.lib.chat.ChatAuth;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author covers1624
 */
public class ChatAuthImpl implements ChatAuth {

    private static final Logger LOGGER = LogManager.getLogger();

    private final UUID uuid;
    private final String uuidHash;

    public ChatAuthImpl(Minecraft mc) {
        uuid = mc.getUser().getProfileId();
        uuidHash = Hashing.sha256().hashString(uuid.toString(), UTF_8).toString().toUpperCase(Locale.ROOT);
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

    @Override
    public @Nullable JWebToken getSessionToken() {
        try {
            return MineTogetherSession.getDefault().getTokenAsync().get();
        } catch (InterruptedException | ExecutionException ex ){
            LOGGER.error("Error whilst waiting for token.", ex);
            return null;
        }
    }
}
