package net.creeperhost.minetogether.lib.chat.profile;

import com.google.common.collect.ImmutableSet;
import net.covers1624.quack.collection.StreamableIterable;
import net.creeperhost.minetogether.lib.chat.MutedUserList;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.request.ProfileResponse;
import net.creeperhost.minetogether.lib.chat.util.HashLength;
import net.creeperhost.minetogether.lib.util.AbstractWeakNotifiable;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static net.creeperhost.minetogether.lib.chat.message.MessageComponent.MESSAGE_DELETED;

/**
 * Created by covers1624 on 22/6/22.
 */
public class Profile extends AbstractWeakNotifiable<Profile> {

    // List of all message sent by the user.
    private final List<Message> sentMessages = new LinkedList<>();
    private final MutedUserList mutedUserList;
    final String initialHash;

    private ImmutableSet<String> aliases;
    @Nullable
    private String fullHash;
    private String displayName;
    private boolean isBanned;
    private boolean isPremium;
    private boolean isFriend;
    private boolean isMuted;

    // Stale on creation.
    private boolean stale = true;

    private boolean updating = false;

    Profile(MutedUserList mutedUserList, String initialHash) {
        this.mutedUserList = mutedUserList;
        this.initialHash = initialHash;
        aliases = ImmutableSet.of(initialHash);

        // We create Profile entries for known users using their full hash.
        if (HashLength.FULL.matches(initialHash)) {
            fullHash = initialHash;
            isMuted = mutedUserList.isUserMuted(fullHash);
        }

        // First 5 characters after MT if exists.
        // TODO can we assert that the first 2 characters will ALWAYS be MT?
        int start = initialHash.startsWith("MT") ? 2 : 0;
        displayName = "User#" + initialHash.substring(start, start + 5);
    }

    public void unmute() {
        assert isMuted;
        assert fullHash != null; // TODO, function to wait for profile to finish updating?

        isMuted = false;
        mutedUserList.unmuteUser(fullHash);
        fire(this);
    }

    public void mute() {
        assert !isMuted;
        assert fullHash != null; // TODO, function to wait for profile to finish updating?

        isMuted = true;
        mutedUserList.muteUser(fullHash);
        fire(this);
    }

    public void banned() {
        assert !isBanned;

        isBanned = true;
        for (Message message : sentMessages) {
            message.setMessageOverride(MESSAGE_DELETED);
        }
        fire(this);
    }

    public void unbanned() {
        assert isBanned;

        isBanned = false;
        for (Message message : sentMessages) {
            message.setMessageOverride(null);
        }
        fire(this);
    }

    public void addSentMessage(Message message) {
        sentMessages.add(message);
    }

    // @formatter:off
    public Set<String> getAliases() { return aliases; }
    public String getDisplayName() { return displayName; }
    public boolean isBanned() { return isBanned; }
    public boolean isPremium() { return isPremium; }
    public boolean isFriend() { return isFriend; }
    public boolean isMuted() { return isMuted; }
    public boolean isUpdating() { return updating; }
    public boolean isStale() { return stale; }
    // @formatter:on

    Consumer<ProfileResponse.ProfileData> onStartUpdating() {
        assert !updating;
        updating = true;
        return p -> {
            aliases = StreamableIterable.of(HashLength.values())
                    .map(e -> e.format(p.getLongHash()))
                    .toImmutableSet();
            fullHash = p.getLongHash();
            displayName = p.getDisplay();
            isPremium = p.isPremium();
            // TODO load more data.

            fire(this);
            updating = false;
            stale = false;
        };
    }
}
