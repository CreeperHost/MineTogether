package net.creeperhost.minetogether;

import java.io.File;

public class DebugHandler
{
    public boolean isDebug;

    public DebugHandler()
    {
        File file = new File("." + File.separator + "local/minetogether/debug.txt");
        isDebug = file.exists();
    }

    public boolean isDebug()
    {
        return isDebug;
    }
}
