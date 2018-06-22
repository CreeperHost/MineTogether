package net.creeperhost.minetogether.common;

import net.creeperhost.minetogether.serverlist.data.Friend;
import org.apache.logging.log4j.Logger;

import java.util.List;

public interface IHost
{
    List<Friend> getFriends();
    void friendEvent(String name, boolean isMessage);

    Logger getLogger();
}
