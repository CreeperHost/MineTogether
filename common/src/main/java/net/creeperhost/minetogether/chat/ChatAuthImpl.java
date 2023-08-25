package net.creeperhost.minetogether.chat;

import com.google.common.hash.Hashing;
import com.mojang.authlib.exceptions.AuthenticationException;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.lib.chat.ChatAuth;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
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
@SuppressWarnings ("UnstableApiUsage")
public class ChatAuthImpl implements ChatAuth {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Minecraft mc;
    private final UUID uuid;
    private final String uuidHash;

    public ChatAuthImpl(Minecraft mc) {
        this.mc = mc;
        uuid = Player.createPlayerUUID(mc.getUser().getGameProfile());
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

    @Deprecated // Exists for old connect. Will be nuked with new connect.
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
