package net.creeperhost.minetogether.lib.chat.request;

import net.creeperhost.minetogether.lib.web.ApiResponse;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by covers1624 on 21/6/22.
 */
public class ProfileResponse extends ApiResponse {

    @Nullable
    private Map<String, ProfileData> profileData;

    public Set<String> getDataKeys() {
        return Collections.unmodifiableSet(Objects.requireNonNull(profileData).keySet());
    }

    @Nullable
    public ProfileData getData(String hash) {
        return Objects.requireNonNull(profileData).get(hash);
    }

    public static class ProfileData {

        @Nullable
        private Map<String, String> hash;
        @Nullable
        private String friendCode;
        @Nullable
        private ChatData chat;
        @Nullable
        private Boolean cached;
        @Nullable
        private String display;
        @Nullable
        private Boolean hasAccount;
        @Nullable
        private Boolean premium;

        public String getLongHash() {
            Map<String, String> hashes = Objects.requireNonNull(hash);
            return Objects.requireNonNull(hashes.get("long"));
        }

        public String getFriendCode() {
            return Objects.requireNonNull(friendCode);
        }

        public boolean isOnline() {
            ChatData chat = Objects.requireNonNull(this.chat);
            return Objects.requireNonNull(chat.online);
        }

        public boolean isCached() {
            return Objects.requireNonNull(cached);
        }

        public String getDisplay() {
            return Objects.requireNonNull(display);
        }

        public boolean hasAccount() {
            return Objects.requireNonNull(hasAccount);
        }

        public boolean isPremium() {
            return Objects.requireNonNull(premium);
        }
    }

    public static class ChatData {

        @Nullable
        private Map<String, String> hash;
        @Nullable
        private Boolean online;
    }
}

