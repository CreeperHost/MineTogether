package net.creeperhost.minetogether.module.connect;

import net.minecraft.client.server.LanServer;

public class LanServerInfoConnect extends LanServer
{
    private final FriendDetector.PendingFriend friend;

    public LanServerInfoConnect(FriendDetector.PendingFriend friend)
    {
        super(friend.getDisplayName(), friend.getAddress());
        this.friend = friend;
    }

    public FriendDetector.PendingFriend getFriend()
    {
        return friend;
    }
}
