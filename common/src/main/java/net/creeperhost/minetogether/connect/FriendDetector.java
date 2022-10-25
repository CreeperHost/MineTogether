package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogetherconnect.ConnectMain;

import java.util.ArrayList;

public class FriendDetector implements Runnable {

    private FriendsServerList owner;

    public FriendDetector(FriendsServerList owner) {
        this.owner = owner;
    }

    @Override
    public void run() {
        try {
            ConnectHandler.FriendsResponse friendsResp = ConnectHandler.getFriendsBlocking();
            if (friendsResp != null && friendsResp.getFriends() != null) {
                ArrayList<ConnectHandler.FriendsResponse.Friend> friends = friendsResp.getFriends();
                for (ConnectHandler.FriendsResponse.Friend friend : friends) {
                    // get profile
                    String address = ConnectMain.getBackendServer().address + ":" + friend.getPort();
                    Profile profile = MineTogetherChat.CHAT_STATE.profileManager.lookupProfile(friend.getHash());
                    PendingFriend server = new PendingFriend(profile, address);
                    owner.addPendingServer(server);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PendingFriend {

        private final Profile profile;
        private final String address;

        public PendingFriend(Profile profile, String address) {
            this.profile = profile;
            this.address = address;
        }

        public String getChosenName() {
            return profile.getFriendName();
        }

        public String getDisplayName() {
            return profile.isFriend() ? profile.getFriendName() : profile.getDisplayName();
        }

        public String getAddress() {
            return address;
        }
    }
}
