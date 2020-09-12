package net.creeperhost.minetogether.data;

import net.creeperhost.minetogether.chat.ChatHandler;

public class Friend
{
    private final String code;
    private final String name;
    private final boolean accepted;

    public Friend(String name, String code, boolean accepted)
    {
        this.code = code;
        this.name = name;
        this.accepted = accepted;
    }

    public String getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

    public boolean isAccepted()
    {
        return accepted;
    }

    public Profile getProfile()
    {
        return ChatHandler.knownUsers.findByHash(code);
    }
}
