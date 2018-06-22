package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.serverlist.data.Friend;

import java.util.List;

public interface IChatHost
{
    List<Friend> getFriends();
    void friendEvent(String name, boolean isMessage);
}
