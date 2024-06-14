package net.creeperhost.minetogether.chat;

import com.google.common.hash.Hashing;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.lib.chat.ChatAuth;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author covers1624
 */
@SuppressWarnings ("UnstableApiUsage")
public class ChatAuthImpl implements ChatAuth {

    private final UUID uuid;
    private final String uuidHash;

    public ChatAuthImpl(Minecraft mc) {
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
    public void resetSessionToken() {
        MineTogetherSession.getDefault().forceResetToken();
    }

    @Override
    public CompletableFuture<JWebToken> getSessionTokenAsync() {
        return MineTogetherSession.getDefault().getTokenAsync();
    }
}
