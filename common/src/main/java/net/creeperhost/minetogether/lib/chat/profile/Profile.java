package net.creeperhost.minetogether.lib.chat.profile;

import com.google.common.collect.ImmutableSet;
import net.covers1624.quack.collection.StreamableIterable;
import net.creeperhost.minetogether.lib.chat.request.ProfileResponse;
import net.creeperhost.minetogether.lib.chat.util.HashLength;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 22/6/22.
 */
public class Profile {

    final String initialHash;

    private ImmutableSet<String> aliases;
    private String displayName;
    private boolean isBanned;

    // Stale on creation.
    private boolean stale = true;

    private boolean updating = false;

    Profile(String initialHash) {
        this.initialHash = initialHash;
        aliases = ImmutableSet.of(initialHash);

        // First 5 characters after MT if exists.
        // TODO can we assert that the first 2 characters will ALWAYS be MT?
        int start = initialHash.startsWith("MT") ? 2 : 0;
        displayName = "User#" + initialHash.substring(start, start + 5);
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public boolean isStale() {
        return stale;
    }

    public boolean isUpdating() {
        return updating;
    }

    Consumer<ProfileResponse.ProfileData> onStartUpdating() {
        assert !updating;
        updating = true;
        return p -> {
            aliases = StreamableIterable.of(HashLength.values())
                    .map(e -> e.format(p.getLongHash()))
                    .toImmutableSet();
            displayName = p.getDisplay();
            // TODO load more data.

            updating = false;
            stale = false;
        };
    }
}