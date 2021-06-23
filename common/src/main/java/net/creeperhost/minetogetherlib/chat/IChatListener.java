package net.creeperhost.minetogetherlib.chat;

import net.creeperhost.minetogetherlib.chat.data.Profile;

public interface IChatListener
{
    void onPartyInvite(Profile profile);

    void onFriendOnline(Profile profile);
}
