package net.creeperhost.minetogether.data;

public class ModPack
{
    String id;
    String name;
    String displayVersion;

    public ModPack(String id, String name, String displayVersion)
    {
        this.id = id;
        this.name = name;
        this.displayVersion = displayVersion;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }

    public String getDisplayVersion()
    {
        return displayVersion;
    }
}
