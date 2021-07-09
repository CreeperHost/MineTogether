package net.creeperhost.minetogether.module.connect;

import java.util.ArrayList;

public class FriendDetector implements Runnable {
    private FriendsServerList owner;
    public FriendDetector(FriendsServerList owner) {
        this.owner = owner;
    }

    @Override
    public void run() {
        ConnectHandler.FriendsResponse friendsResp = ConnectHandler.getFriendsBlocking();
        if (friendsResp != null && friendsResp.getFriends() != null) {
            ArrayList<ConnectHandler.FriendsResponse.Friend> friends = friendsResp.getFriends();
            for (ConnectHandler.FriendsResponse.Friend friend : friends) {
                PendingFriend server = new PendingFriend(friend.getChosenName(), friend.getDisplayName(), friend.getAddress());
                owner.addPendingServer(server);
            }
        }
    }

    public static class PendingFriend
    {
        private final String chosenName;
        private final String displayName;
        private final String address;

        public PendingFriend(String chosenName, String displayName, String address)
        {
            this.chosenName = chosenName;
            this.displayName = displayName;
            this.address = address;
        }

        public String getChosenName() {
            return chosenName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAddress() {
            return address;
        }
    }
}
