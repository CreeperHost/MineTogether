package net.creeperhost.minetogether.lib.chat.profile;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.lib.chat.request.ProfileRequest;
import net.creeperhost.minetogether.lib.chat.request.ProfileResponse;
import net.creeperhost.minetogether.lib.chat.request.ProfileResponse.ProfileData;
import net.creeperhost.minetogether.lib.web.ApiClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 22/6/22.
 */
public class ProfileManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ExecutorService PROFILE_EXECUTOR = Executors.newFixedThreadPool(2,
            new ThreadFactoryBuilder()
                    .setNameFormat("MT Profile Update Thread %d")
                    .setDaemon(true)
                    .build()
    );

    private final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(2,
            new ThreadFactoryBuilder()
                    .setNameFormat("MT Scheduled Profile Update Thread %d")
                    .setDaemon(true)
                    .build()
    );

    private final ApiClient apiClient;
    private final Map<String, Profile> profiles = new HashMap<>();

    public ProfileManager(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Profile lookupProfile(String hash) {
        Profile profile = profiles.get(hash);
        if (profile == null) {
            synchronized (profiles) {
                // Double-check after lock, we may not need to do anything.
                profile = profiles.get(hash);
                if (profile != null) return profile;
                profile = new Profile(hash);
                profiles.put(hash, profile);
            }
        }
        if (profile.isStale() && !profile.isUpdating()) {
            scheduleUpdate(profile);
        }
        return profile;
    }

    private void scheduleUpdate(Profile profile) {
        Consumer<ProfileData> onFinished = profile.onStartUpdating()
                .andThen(p -> updateAliases(profile));

        scheduleUpdate(profile, onFinished, 0);
    }

    private void scheduleUpdate(Profile profile, Consumer<ProfileData> onFinished, int depth) {
        PROFILE_EXECUTOR.execute(() -> {
            try {
                ProfileResponse resp = apiClient.execute(new ProfileRequest(profile.initialHash)).apiResponse();
                if (resp.getStatus().equals("success")) {
                    onFinished.accept(resp.getData(profile.initialHash));
                    return;
                }
                if (!resp.getMessage().startsWith("Profile request already ongoing")) {
                    LOGGER.warn("Unexpected error response from API: " + resp.getMessage());
                }
                // TODO how should we handle profile never updating? Bail completely?
            } catch (IOException ex) {
                LOGGER.warn("IOException whilst querying profile.", ex);
            }
            // Try again based on depth. 1 -> 2 -> 3 -> 4, minutes
            SCHEDULED_EXECUTOR.schedule(() -> scheduleUpdate(profile, onFinished, depth + 1), depth + 1, TimeUnit.MINUTES);
        });
    }

    private void updateAliases(Profile profile) {
        synchronized (profiles) {
            for (String alias : profile.getAliases()) {
                if (alias.equals(profile.initialHash)) continue;
                Profile other = profiles.put(alias, profile);

                if (other != null && other != profile) {
                    // What? oookay then.
                    LOGGER.warn("Duplicate profiles with hash {}. A: {}, B: {}", alias, other.initialHash, profile.initialHash);
                }
            }
        }
    }
}
