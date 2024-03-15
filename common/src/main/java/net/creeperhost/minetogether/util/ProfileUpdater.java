package net.creeperhost.minetogether.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.server.Services;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 15/3/24.
 */
public class ProfileUpdater {

    private static LoadingCache<UUID, CompletableFuture<@Nullable GameProfile>> profileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(256)
            .build(new CacheLoader<>() {
                @Override
                public CompletableFuture<GameProfile> load(UUID key) {
                    return loadProfile(key);
                }
            });

    /**
     * Update a profile with all information, including textures.
     *
     * @param uuid     The UUID to query.
     * @param callback Callback to be fired when the full data is available.
     */
    public static void updateProfile(UUID uuid, Consumer<GameProfile> callback) {
        profileCache.getUnchecked(uuid)
                .thenAcceptAsync(e -> {
                    if (e != null) {
                        callback.accept(e);
                    }
                }, Minecraft.getInstance());
    }

    private static CompletableFuture<@Nullable GameProfile> loadProfile(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ProfileResult result = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(uuid, true);
            if (result == null) return null;
            return result.profile();
        }, Util.backgroundExecutor());
    }
}
