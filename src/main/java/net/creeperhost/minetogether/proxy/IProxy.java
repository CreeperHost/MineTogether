package net.creeperhost.minetogether.proxy;

import net.creeperhost.minetogether.chat.Message;

import java.util.UUID;

public interface IProxy
{
    void registerKeys();

    void openFriendsGui();

    UUID getUUID();

    void startChat();

    void disableIngameChat();

    void enableIngameChat();

    void closeGroupChat();

    void messageReceived(String target, Message messagePair);

    void updateChatChannel();

    void refreshChat();

    boolean checkOnline();
}
