package net.creeperhost.minetogetherlib.chat;

import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;

public interface IChatListener
{
    void onPartyInvite(Profile profile);

    void onFriendOnline(Profile profile);

    void onFriendAccept(String name);

    String onServerIdRequest();

    void sendMessage(Message message);

    void setHasNewMessage(boolean value);
}
