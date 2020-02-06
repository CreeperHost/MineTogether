package net.creeperhost.minetogether.chat;

public class Message
{
    public long timeReceived;
    public String messageStr;
    public String sender;

    public Message(long timeReceived, String sender, String messageStr)
    {
        this.timeReceived = timeReceived;
        this.messageStr = messageStr;
        this.sender = sender;
    }
}
