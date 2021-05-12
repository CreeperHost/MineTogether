package net.creeperhost.minetogetherlib.chat.data;

public class PrivateChat
{
    String channelname;
    String owner;
    
    public PrivateChat(String channelname, String owner)
    {
        this.channelname = channelname;
        this.owner = owner;
    }
    
    public String getChannelname()
    {
        return channelname;
    }
    
    public String getOwner()
    {
        return owner;
    }
}
