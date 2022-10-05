package net.creeperhost.minetogether.lib.chat.request;

import net.creeperhost.minetogether.lib.web.ApiResponse;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by covers1624 on 7/9/22.
 */
public class ListFriendsResponse extends ApiResponse {

    public final List<FriendEntry> friends = new LinkedList<>();

    public static class FriendEntry {

        @Nullable
        private String name;
        @Nullable
        private Boolean accepted;
        @Nullable
        private String hash;

        public String getName() {
            return Objects.requireNonNull(name);
        }

        public boolean isAccepted() {
            return Objects.requireNonNull(accepted);
        }

        public String getHash() {
            return Objects.requireNonNull(hash);
        }
    }
}
