package net.creeperhost.minetogether.handler;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.serverlists.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;

import java.util.List;

public class AutoServerConnectHandler
{
    static boolean first = true;

    //This is Aaron code and was copy pasta
    public static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
    {
        if (screen instanceof TitleScreen && first)
        {
            first = false;
            String server = System.getProperty("mt.server");
            int serverId = -1;
            if (server != null)
            {
                try
                {
                    serverId = Integer.parseInt(server);
                } catch (Throwable t)
                {
                    MineTogether.logger.error("Unable to auto connect to server as unable to parse server ID");
                }

                Server serverObj = ChatCallbacks.getServer(serverId);

                if (serverObj != null)
                {
                    String[] serverSplit = serverObj.host.split(":");

                    int realPort = -1;
                    try
                    {
                        realPort = Integer.parseInt(serverSplit[1]);
                    } catch (Throwable t)
                    {
                        MineTogether.logger.error("Unable to auto connect to server as unable to parse server port for ID " + serverId);
                    }

                    if (realPort != -1)
                    {
                        ServerData serverData = new ServerData(serverSplit[0], String.valueOf(realPort), false);
                        Minecraft.getInstance().setScreen(new ConnectScreen(screen, Minecraft.getInstance(), serverData));
                    }
                }
            }
        }
    }
}
