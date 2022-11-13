package net.creeperhost.minetogether.lib.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.covers1624.quack.gson.JsonUtils;
import net.covers1624.quack.io.IOUtils;
import net.creeperhost.minetogether.lib.chat.annotation.HashLen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.creeperhost.minetogether.lib.chat.util.HashLength.FULL;

/**
 * Responsible for managing the Muted user list.
 * <p>
 * Created by covers1624 on 22/8/22.
 */
public class MutedUserList {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private static final Type STRING_SET = new TypeToken<Set<String>>() { }.getType();

    private final Path file;
    private final Set<String> mutedUsers;

    @VisibleForTesting
    protected MutedUserList(Path file, Set<String> mutedUsers) {
        this.file = file;
        this.mutedUsers = mutedUsers;
    }

    public MutedUserList(Path file) {
        this.file = file;
        Set<String> mu;
        if (Files.exists(file)) {
            try {
                mu = JsonUtils.parse(GSON, file, STRING_SET);
            } catch (IOException ex) {
                LOGGER.error("Failed to load MutedUsers list!", ex);
                mu = new HashSet<>();
            }
        } else {
            mu = new HashSet<>();
        }
        mutedUsers = mu;
        mutedUsers.remove(null);
        save();
    }

    /**
     * Checks if the user with the given hash is currently muted.
     *
     * @param hash The hash to check.
     * @return If the user is muted.
     */
    public boolean isUserMuted(@HashLen (FULL) String hash) {
        assert FULL.matches(hash);

        return mutedUsers.contains(hash);
    }

    /**
     * Adds this user to the list of currently muted users.
     *
     * @param hash The hash of the user to mute.
     */
    public void muteUser(@HashLen (FULL) String hash) {
        assert FULL.matches(hash);

        if (mutedUsers.add(hash)) {
            save();
        }
    }

    /**
     * Removes this user from the list of currently muted users.
     *
     * @param hash The hash of the user to unmute.
     */
    public void unmuteUser(@HashLen (FULL) String hash) {
        assert FULL.matches(hash);

        if (mutedUsers.remove(hash)) {
            save();
        }
    }

    /**
     * Get an immutable iterable view of all muted user hashes.
     *
     * @return The muted user hashes.
     */
    @HashLen (FULL)
    public Iterable<String> getMutedUsers() {
        return Collections.unmodifiableSet(mutedUsers);
    }

    @VisibleForTesting
    protected void save() {
        try {
            JsonUtils.write(GSON, IOUtils.makeParents(file), mutedUsers);
        } catch (IOException ex) {
            LOGGER.warn("Failed to save MutedUsers list!", ex);
        }
    }
}
