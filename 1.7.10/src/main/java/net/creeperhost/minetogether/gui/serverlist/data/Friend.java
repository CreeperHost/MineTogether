package net.creeperhost.minetogether.gui.serverlist.data;

public class Friend
{
    private final String name;
    private final String code;
    private final boolean accepted;

    public Friend(String name, String code, boolean accepted)
    {
        this.name = name;
        this.code = code;
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
}
