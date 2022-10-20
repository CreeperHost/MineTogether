package net.creeperhost.minetogether.lib.chat.profile;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.covers1624.quack.collection.StreamableIterable;
import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.irc.IrcUser;
import net.creeperhost.minetogether.lib.chat.request.*;
import net.creeperhost.minetogether.lib.chat.request.ProfileResponse.ProfileData;
import net.creeperhost.minetogether.lib.chat.util.HashLength;
import net.creeperhost.minetogether.lib.util.AbstractWeakNotifiable;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 22/6/22.
 */
public class ProfileManager extends AbstractWeakNotifiable<ProfileManager.ProfileManagerEvent> {

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

    private final ChatState chatState;
    private final Map<String, Profile> profiles = new HashMap<>();
    private final Profile ownProfile;

    private final List<FriendRequest> friendRequests = new LinkedList<>();

    public ProfileManager(ChatState chatState, String ownHash) {
        this.chatState = chatState;
        for (String mutedUser : chatState.mutedUserList.getMutedUsers()) {
            lookupProfileStale(mutedUser);
        }
        ownProfile = lookupProfile(ownHash);
    }

    /**
     * Gets our own profile.
     *
     * @return Our profile.
     */
    public Profile getOwnProfile() {
        return ownProfile;
    }

    /**
     * Looks up another user's Profile.
     * <p>
     * This method will request an update of the profile's data. Ban status,
     * Nickname, etc.
     *
     * @param hash The first seen hash of the other user. Might be any length.
     * @return The Profile.
     */
    public Profile lookupProfile(String hash) {
        Profile profile = lookupProfileStale(hash);
        if (profile.isStale() && !profile.isUpdating()) {
            scheduleUpdate(profile);
        }
        return profile;
    }

    /**
     * Looks up another user's Profile.
     * <p>
     * In contrast to {@link #lookupProfile} this method only creates the singleton Profile.
     * <p>
     * It does not schedule an update of profile data.
     *
     * @param hash The first seen hash of the other user. Might be any length.
     * @return The Profile.
     */
    public Profile lookupProfileStale(String hash) {
        Profile profile = profiles.get(hash);
        if (profile == null) {
            synchronized (profiles) {
                // Double-check after lock, we may not need to do anything.
                profile = profiles.get(hash);
                if (profile != null) return profile;

                // If we have a full hash we can do some special lookups to try and find the others.
                if (HashLength.FULL.matches(hash)) {
                    for (String alias : Profile.computeAllAliases(hash)) {
                        profile = profiles.get(alias);
                        if (profile != null) {
                            // Add faster lookup.
                            profiles.put(hash, profile);
                            return profile;
                        }
                    }
                }

                profile = new Profile(chatState, hash);
                profiles.put(hash, profile);
                updateAliases(profile);
            }
        }
        return profile;
    }

    /**
     * Gets an immutable copy of all profiles seen/known at the current moment.
     * <p>
     * Please use sparely, this creates a new List!
     *
     * @return The Profiles of the currently known users.
     */
    public List<Profile> getKnownProfiles() {
        synchronized (profiles) {
            // Since we use a synchronized lock on profiles, it is important
            // that we don't return an Iterable backed by profiles, thus we copy.
            return StreamableIterable.of(profiles.values())
                    .distinct()                             // Entries may exist more than once because of aliases.
                    .toImmutableList();
        }
    }

    /**
     * Gets an immutable copy of all profiles currently muted.
     *
     * @return The Profiles of the currently muted users.
     */
    public List<Profile> getMutedProfiles() {
        synchronized (profiles) {
            // We could return the StreamableIterable here, but as above, it is important
            // that we don't return an Iterable backed by profiles in any way, thus we terminate stream.
            return StreamableIterable.of(profiles.values())
                    .distinct()                             // Entries may exist more than once because of aliases.
                    .filter(Profile::isMuted)
                    .toImmutableList();
        }
    }

    /**
     * Gets all the pending friend requests.
     *
     * @return The pending friend requests.
     */
    public List<FriendRequest> getFriendRequests() {
        synchronized (friendRequests) {
            return ImmutableList.copyOf(friendRequests);
        }
    }

    public boolean sendFriendRequest(Profile to, String desiredName) {
        IrcUser ircUser = chatState.ircClient.getUser(to);
        if (ircUser == null) return false; // TODO assertion?

        // Send friend request.
        ircUser.sendFriendRequest(ownProfile.getFriendCode(), desiredName);
        return true;
    }

    public void denyFriendRequest(FriendRequest request) {
        synchronized (friendRequests) {
            friendRequests.remove(request);
        }
    }

    public boolean acceptFriendRequest(FriendRequest request, String desiredName) {
        IrcUser ircUser = chatState.ircClient.getUser(request.from);
        if (ircUser == null) return false; // TODO assertion?

        synchronized (friendRequests) {
            friendRequests.remove(request);
        }

        ircUser.acceptFriendRequest(ownProfile.getFriendCode(), request.desiredName);
        apiAcceptFriendRequest(request.friendCode, desiredName);
        return true;
    }

    public void onFriendRequestAccepted(Profile from, String friendCode, String desiredName) {
        fire(new ProfileManagerEvent(EventType.FRIEND_REQUEST_ACCEPTED, from));

        apiAcceptFriendRequest(friendCode, desiredName);
    }

    // incoming new friend request from IRC.
    public void onIncomingFriendRequest(Profile from, String friendCode, String desiredName) {
        synchronized (friendRequests) {
            FriendRequest request = new FriendRequest(from, friendCode, desiredName);
            friendRequests.add(request);
            fire(new ProfileManagerEvent(EventType.FRIEND_REQUEST_ADDED, request));
        }
    }

    public void apiAcceptFriendRequest(String friendCode, String desiredName) {
        try {
            ApiResponse resp = chatState.api.execute(new AddFriendRequest(
                    ownProfile.getFullHash(),
                    friendCode,
                    desiredName
            )).apiResponse();
            if (!resp.getStatus().equals("success")) {
                LOGGER.error("Failed to remove friend. Api returned: {}", resp.getMessageOrNull());
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to add Friend.", ex);
        }
    }

    public void updateFriends() {
        try {
            // TODO we need to purge removed friends somehow.
            ListFriendsResponse resp = chatState.api.execute(new ListFriendsRequest(getOwnProfile().getFullHash())).apiResponse();
            for (ListFriendsResponse.FriendEntry friend : resp.friends) {
                if (friend.isAccepted()) {
                    Profile profile = lookupProfile(friend.getHash());
                    profile.setFriend(friend.getName());
                }
            }
        } catch (Throwable ex) {
            LOGGER.error("Failed to query friend list.", ex);
        }
    }

    public void removeFriend(Profile friend) {
        friend.removeFriend();
        try {
            ApiResponse resp = chatState.api.execute(new RemoveFriendRequest(friend.getFriendCode(), ownProfile.getFullHash())).apiResponse();
            if (!resp.getStatus().equals("success")) {
                LOGGER.error("Failed to remove friend. Api returned: {}", resp.getMessageOrNull());
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to remove friend!", ex);
        }
    }

    private void scheduleUpdate(Profile profile) {
        Consumer<ProfileData> onFinished = profile.onStartUpdating()
                .andThen(p -> updateAliases(profile));

        scheduleUpdate(profile, onFinished, 0);
    }

    private void scheduleUpdate(Profile profile, Consumer<ProfileData> onFinished, int depth) {
        PROFILE_EXECUTOR.execute(() -> {
            try {
                ProfileResponse resp = chatState.api.execute(new ProfileRequest(profile.initialHash)).apiResponse();
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

    public static class FriendRequest {

        // The user this request is from.
        public final Profile from;
        // The friend code of 'from'
        public final String friendCode;
        // The name for us, to be sent back to the sender.
        public final String desiredName;

        public FriendRequest(Profile from, String friendCode, String desiredName) {
            this.from = from;
            this.friendCode = friendCode;
            this.desiredName = desiredName;
        }
    }

    public static class ProfileManagerEvent {

        public final EventType type;
        @Nullable
        public final Object data;

        public ProfileManagerEvent(EventType type, @Nullable Object data) {
            this.type = type;
            this.data = data;
        }
    }

    public enum EventType {
        FRIEND_REQUEST_ADDED,
        FRIEND_REQUEST_ACCEPTED,
    }
}
