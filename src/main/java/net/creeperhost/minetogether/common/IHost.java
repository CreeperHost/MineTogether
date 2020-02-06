package net.creeperhost.minetogether.common;

import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.data.Friend;
import org.apache.logging.log4j.Logger;

import java.util.List;

public interface IHost
{
    String getNameForUser(String nick);

    List<Friend> getFriends();

    void friendEvent(String name, boolean isMessage);

    Logger getLogger();

    void messageReceived(String target, Message messagePair);

    String getFriendCode();

    void acceptFriend(String s, String trim);

    void closeGroupChat();

    void updateChatChannel();

    void userBanned(String username);
}
