package net.creeperhost.minetogether.proxy;

import net.creeperhost.minetogether.chat.Message;

import java.util.UUID;

public class Server implements IProxy
{
    @Override
    public void registerKeys()
    {
    }
    
    @Override
    public void openFriendsGui()
    {
    }
    
    @Override
    public UUID getUUID()
    {
        return null;
    }

    @Override
    public void reCacheUUID()
    {
    }

    @Override
    public void startChat()
    {
    }

    @Override
    public void stopChat()
    {

    }

    @Override
    public void disableIngameChat()
    {
    }
    
    @Override
    public void enableIngameChat()
    {
    }
    
    @Override
    public void closeGroupChat()
    {
    }
    
    @Override
    public void messageReceived(String target, Message messagePair)
    {
    }
    
    @Override
    public void updateChatChannel()
    {
    }
    
    @Override
    public void refreshChat()
    {
    }
    
    @Override
    public boolean checkOnline()
    {
        return false;
    }

    @Override
    public String getServerIDAndVerify() { return null; }
}
