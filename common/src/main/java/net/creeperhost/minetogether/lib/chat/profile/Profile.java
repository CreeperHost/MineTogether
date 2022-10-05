package net.creeperhost.minetogether.lib.chat.profile;

import com.google.common.collect.ImmutableSet;
import net.covers1624.quack.collection.StreamableIterable;
import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.request.ProfileResponse;
import net.creeperhost.minetogether.lib.chat.util.HashLength;
import net.creeperhost.minetogether.lib.util.AbstractWeakNotifiable;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static net.creeperhost.minetogether.lib.chat.message.MessageComponent.MESSAGE_DELETED;

/**
 * Created by covers1624 on 22/6/22.
 */
public class Profile extends AbstractWeakNotifiable<Profile.ProfileEvent> {

    // List of all message sent by the user.
    private final List<Message> sentMessages = new LinkedList<>();
    private final ChatState chatState;
    final String initialHash;

    private ImmutableSet<String> aliases;
    @Nullable
    private String fullHash;
    private String displayName;
    @Nullable
    private String friendCode;
    @Nullable
    private String friendName;
    private boolean isBanned;
    private boolean isPremium;
    private boolean isFriend;
    private boolean isMuted;
    private boolean isOnline;

    // Stale on creation.
    private boolean stale = true;

    private boolean updating = false;

    Profile(ChatState chatState, String initialHash) {
        this.chatState = chatState;
        this.initialHash = initialHash;

        // We create Profile entries for known users using their full hash.
        if (HashLength.FULL.matches(initialHash)) {
            fullHash = initialHash;
            isMuted = chatState.mutedUserList.isUserMuted(fullHash);
            aliases = computeAllAliases(fullHash);
        } else {
            aliases = ImmutableSet.of(initialHash);
        }

        // First 5 characters after MT if exists.
        int start = initialHash.startsWith("MT") ? 2 : 0;
        displayName = "User#" + initialHash.substring(start, start + 5);
    }

    public void unmute() {
        assert isMuted;
        assert fullHash != null; // TODO, function to wait for profile to finish updating?

        isMuted = false;
        chatState.mutedUserList.unmuteUser(fullHash);
        fire(new ProfileEvent(EventType.UNMUTED, this));
    }

    public void mute() {
        assert !isMuted;
        assert fullHash != null; // TODO, function to wait for profile to finish updating?

        isMuted = true;
        chatState.mutedUserList.muteUser(fullHash);
        fire(new ProfileEvent(EventType.MUTED, this));
    }

    public void banned() {
        assert !isBanned;

        isBanned = true;
        for (Message message : sentMessages) {
            message.setMessageOverride(MESSAGE_DELETED);
        }
        fire(new ProfileEvent(EventType.BANNED, this));
    }

    public void unbanned() {
        assert isBanned;

        isBanned = false;
        for (Message message : sentMessages) {
            message.setMessageOverride(null);
        }
        fire(new ProfileEvent(EventType.UNBANNED, this));
    }

    public void addSentMessage(Message message) {
        sentMessages.add(message);
    }

    public void setFriend(String name) {
        isFriend = true;
        friendName = name;
    }

    void removeFriend() {
        isFriend = false;
        friendName = null;
    }

    // @formatter:off
    public Set<String> getAliases() { return aliases; }
    public String getDisplayName() { return displayName; }
    public String getFriendCode() { return Objects.requireNonNull(friendCode); }
    public String getFriendName() { return Objects.requireNonNull(friendName); }
    public String getFullHash() { return Objects.requireNonNull(fullHash, "Profile not updated yet."); } // TODO await for profile to have this.
    public boolean isBanned() { return isBanned; }
    public boolean isPremium() { return isPremium; }
    public boolean isFriend() { return isFriend; }
    public boolean isMuted() { return isMuted; }
    public boolean isOnline() { return isOnline; }
    public boolean isUpdating() { return updating; }
    public boolean isStale() { return stale; }
    // @formatter:on

    Consumer<ProfileResponse.ProfileData> onStartUpdating() {
        assert !updating;
        updating = true;
        return p -> {
            aliases = computeAllAliases(p.getLongHash());
            fullHash = p.getLongHash();
            displayName = p.getDisplay();
            friendCode = p.getFriendCode();
            isPremium = p.isPremium();
            // TODO load more data.

            fire(new ProfileEvent(EventType.FULL_PROFILE, this));
            updating = false;
            stale = false;
        };
    }

    private static ImmutableSet<String> computeAllAliases(String fullHash) {
        return StreamableIterable.of(HashLength.values())
                .flatMap(e -> asList(e.format(fullHash), "MT" + e.format(fullHash)))
                .toImmutableSet();
    }

    public static class ProfileEvent {

        public final EventType type;
        public final Profile profile;
        @Nullable
        public final Object data;

        public ProfileEvent(EventType type, Profile profile) {
            this(type, profile, null);
        }

        public ProfileEvent(EventType type, Profile profile, @Nullable Object data) {
            this.type = type;
            this.profile = profile;
            this.data = data;
        }
    }

    public enum EventType {
        MUTED,
        UNMUTED,
        BANNED,
        UNBANNED,
        FULL_PROFILE,
        FRIEND_REQUEST,
    }
}
