package net.creeperhost.minetogether.serverlist.data;

import net.creeperhost.minetogether.Profile;

public class Friend
{
    private final Profile profile;
    private final String code;
    private final String name;
    private final boolean accepted;

    public Friend(Profile profile, String name, String code, boolean accepted)
    {
        this.profile = profile;
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

    public Profile getProfile() {
        return profile;
    }
}
