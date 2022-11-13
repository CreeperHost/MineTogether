package net.creeperhost.minetogether.lib.chat.profile;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.covers1624.quack.collection.StreamableIterable;
import net.covers1624.quack.gson.JsonUtils;
import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.request.ProfileResponse;
import net.creeperhost.minetogether.lib.chat.util.HashLength;
import net.creeperhost.minetogether.lib.util.AbstractWeakNotifiable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static net.creeperhost.minetogether.lib.chat.message.MessageComponent.MESSAGE_DELETED;

/**
 * Created by covers1624 on 22/6/22.
 */
public class Profile extends AbstractWeakNotifiable<Profile.ProfileEvent> {

    private static final Logger LOGGER = LogManager.getLogger();

    // List of all message sent by the user.
    private final List<Message> sentMessages = new LinkedList<>();
    private final ChatState chatState;
    final String initialHash;

    private ImmutableSet<String> aliases;
    @Nullable
    private String fullHash;
    @Nullable
    private String ircHash;
    private String displayName;
    @Nullable
    private String friendCode;
    @Nullable
    private String friendName;
    private boolean hasAccount;
    private boolean isBanned;
    private boolean isPremium;
    private boolean isFriend;
    private boolean isMuted;
    boolean isOnline;

    // Stale on creation.
    private boolean stale = true;

    private boolean updating = false;

    private String packId = "";

    // Set to true when we have performed a whois on the user.
    public boolean newProfileWhoDis;

    Profile(ChatState chatState, String initialHash) {
        this.chatState = chatState;
        this.initialHash = initialHash;

        // We create Profile entries for known users using their full hash.
        if (HashLength.FULL.matches(initialHash)) {
            fullHash = initialHash;
            isMuted = chatState.mutedUserList.isUserMuted(fullHash);
            aliases = computeAllAliases(fullHash);
            ircHash = HashLength.MEDIUM.format(fullHash);
        } else {
            Set<String> aliases = new HashSet<>();
            aliases.add(initialHash);
            if (initialHash.startsWith("MT")) {
                String sub = initialHash.substring(2);
                assert StreamableIterable.of(HashLength.values()).anyMatch(e -> e.matches(sub));
                aliases.add(sub);
            }
            this.aliases = ImmutableSet.copyOf(aliases);
        }

        boolean startsWithMT = initialHash.startsWith("MT");
        String hash = startsWithMT ? initialHash.substring(2) : initialHash;
        if (HashLength.matchesAny(hash)) {
            displayName = "User#" + hash.substring(0, 5);
        } else {
            displayName = hash;
        }
    }

    public void unmute() {
        if (!isMuted) return;
        if (fullHash == null) {
            // TODO, function to wait for profile to finish updating and perform this action.
            LOGGER.warn("Full hash not available when trying to unmute user.");
            return;
        }

        isMuted = false;
        chatState.mutedUserList.unmuteUser(fullHash);
        fire(new ProfileEvent(EventType.UNMUTED, this));
    }

    public void mute() {
        if (isMuted) return;
        if (fullHash == null) {
            // TODO, function to wait for profile to finish updating and perform this action.
            LOGGER.warn("Full hash not available when trying to mute user.");
            return;
        }

        isMuted = true;
        chatState.mutedUserList.muteUser(fullHash);
        fire(new ProfileEvent(EventType.MUTED, this));
    }

    public void banned() {
        if (isBanned) return;

        isBanned = true;
        for (Message message : sentMessages) {
            message.setMessageOverride(MESSAGE_DELETED);
        }
        fire(new ProfileEvent(EventType.BANNED, this));
    }

    public void unbanned() {
        if (!isBanned) return;

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
        fire(new ProfileEvent(EventType.FRIEND_ADD, this));
    }

    void removeFriend() {
        isFriend = false;
        friendName = null;
        fire(new ProfileEvent(EventType.FRIEND_REMOVE, this));
    }

    public void markStale() {
        stale = true;
        newProfileWhoDis = false;
    }

    public void setPack(String realName) {
        if (StringUtils.isNotEmpty(realName) && realName.startsWith("{") && realName.endsWith("}")) {
            try {
                JsonObject obj = JsonUtils.parseRaw(realName).getAsJsonObject();
                String packId = JsonUtils.getString(obj, "p", null);
                if (packId != null && !packId.equals("-1")) {
                    this.packId = packId;
                    return;
                }
            } catch (Throwable ex) {
                LOGGER.error("Failed to parse realName: '{}'", realName);
            }
        }
        packId = "";
    }

    public boolean isOnSamePack(Profile other) {
        return !packId.isEmpty() && packId.equals(other.getPackId());
    }

    // @formatter:off
    public Set<String> getAliases() { return aliases; }
    public String getDisplayName() { return displayName; }
    public String getFriendCode() { return Objects.requireNonNull(friendCode); }
    public String getFriendName() { return Objects.requireNonNull(friendName); }
    public String getFullHash() { return Objects.requireNonNull(fullHash, "Profile not updated yet."); } // TODO await for profile to have this.
    @Nullable public String getIrcName() { return ircHash; }
    public boolean hasAccount() { return hasAccount; }
    public boolean isBanned() { return isBanned; }
    public boolean isPremium() { return isPremium; }
    public boolean isFriend() { return isFriend; }
    public boolean isMuted() { return isMuted; }
    public boolean isOnline() { return isOnline; }
    public boolean isUpdating() { return updating; }
    public boolean isStale() { return stale; }
    public String getPackId() { return packId; }
    // @formatter:on

    Consumer<ProfileResponse.ProfileData> onStartUpdating() {
        assert !updating;
        updating = true;
        return p -> {
            aliases = computeAllAliases(p.getLongHash());
            fullHash = p.getLongHash();
            ircHash = HashLength.MEDIUM.format(fullHash);
            displayName = p.getDisplay();
            friendCode = p.getFriendCode();
            hasAccount = p.hasAccount();
            isPremium = p.isPremium();
            // TODO load more data.

            fire(new ProfileEvent(EventType.FULL_PROFILE, this));
            updating = false;
            stale = false;
        };
    }

    public static ImmutableSet<String> computeAllAliases(String fullHash) {
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
        FRIEND_ADD,
        FRIEND_REMOVE,
        BANNED,
        UNBANNED,
        FULL_PROFILE,
        FRIEND_REQUEST;

        public boolean canChangeName() {
            return this == FULL_PROFILE || this == FRIEND_ADD || this == FRIEND_REMOVE;
        }
    }
}
