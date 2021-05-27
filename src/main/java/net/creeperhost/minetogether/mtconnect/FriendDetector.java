package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.multiplayer.ServerData;

public class FriendDetector implements Runnable {
    private FriendsServerList owner;
    public FriendDetector(FriendsServerList owner) {
        this.owner = owner;
    }

    @Override
    public void run() {
        ConnectHandler.FriendsResponse friends = ConnectHandler.getFriendsBlocking();
        if (friends != null && friends.getFriends() != null) {
            for (ConnectHandler.FriendsResponse.Friend friend : friends.getFriends()) {
                ServerData server = new ServerData(friend.getDisplayName() + "'s world", friend.getAddress(), false);
                owner.addPendingServer(server);
            }
        }
    }
}
