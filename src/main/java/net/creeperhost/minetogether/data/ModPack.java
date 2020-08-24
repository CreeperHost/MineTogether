package net.creeperhost.minetogether.data;

public class ModPack
{
    String id;
    String name;
    String displayVersion;
    String displayIcon;
    
    public ModPack(String id, String name, String displayVersion, String displayIcon)
    {
        this.id = id;
        this.name = name;
        this.displayVersion = displayVersion;
        this.displayIcon = displayIcon;
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

    public String getDisplayIcon() {
        return displayIcon;
    }
}
