package net.creeperhost.minetogether.proxy;

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
}
