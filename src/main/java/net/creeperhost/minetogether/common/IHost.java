package net.creeperhost.minetogether.common;

import net.creeperhost.minetogether.serverlist.data.Friend;
import org.apache.logging.log4j.Logger;

import java.util.List;

public interface IHost
{
    String getNameForUser(String nick);

    List<Friend> getFriends();
    void friendEvent(String name, boolean isMessage);

    Logger getLogger();

    void messageReceived(String target, Pair messagePair);

    String getFriendCode();
}
