package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.network.LanServerInfo;

public class LanServerInfoConnect extends LanServerInfo {
    private final FriendDetector.PendingFriend friend;
    public LanServerInfoConnect(FriendDetector.PendingFriend friend) {
        super(friend.getDisplayName(), friend.getAddress());
        this.friend = friend;
    }

    public FriendDetector.PendingFriend getFriend() {
        return friend;
    }
}
