package net.creeperhost.minetogether.module.multiplayer;

import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.multiplayer.screen.JoinMultiplayerScreenPublic;
import net.creeperhost.minetogether.module.multiplayer.screen.ServerTypeScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class MultiPlayerModule
{
    public static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets)
    {
        if (screen instanceof JoinMultiplayerScreen)
        {
            JoinMultiplayerScreen multiplayerScreen = (JoinMultiplayerScreen) screen;
            //Clean up the buttons in the screen to allow us to add ours
            updateMultiPlayerScreenButtons(multiplayerScreen, abstractWidgets);

            //Don't add the serverlist button if the multiplayer screen is ours
            if (!(screen instanceof JoinMultiplayerScreenPublic))
            {
                Button serverListButton = new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether.multiplayer.serverlist"), p -> Minecraft.getInstance().setScreen(new ServerTypeScreen(screen)));

                ScreenHooks.addRenderableWidget(screen, serverListButton);
                serverListButton.active = !Config.getInstance().getFirstConnect();

                ScreenHooks.addRenderableWidget(screen, new ButtonMultiple(screen.width - 125, 5, 1, Constants.WIDGETS_LOCATION, p ->
                {
                    if (Config.getInstance().isChatEnabled())
                    {
                        Minecraft.getInstance().setScreen(new ChatScreen(screen));
                    }
                    else
                    {
                        Minecraft.getInstance().setScreen(new SettingsScreen(screen));
                    }
                }));
            }
        }
    }

    public static void updateMultiPlayerScreenButtons(JoinMultiplayerScreen multiplayerScreen, List<AbstractWidget> abstractWidgets)
    {
        try
        {
            ScreenHelpers.findButton(I18n.get("selectServer.select"), abstractWidgets).x += 1;

            ScreenHelpers.removeButton(I18n.get("selectServer.refresh"), abstractWidgets);

            AbstractWidget addButton = ScreenHelpers.findButton(I18n.get("selectServer.add"), abstractWidgets);
            if (addButton != null)
            {
                addButton.x -= 104;
                addButton.setWidth(addButton.getWidth() + 27);
            }

            AbstractWidget edit = ScreenHelpers.findButton(I18n.get("selectServer.edit"), abstractWidgets);
            if (edit != null)
            {
                edit.setWidth(edit.getWidth() - 9);
                edit.x += 1;
            }

            AbstractWidget delete = ScreenHelpers.findButton(I18n.get("selectServer.delete"), abstractWidgets);
            if (delete != null)
            {
                delete.x -= 16;
                delete.setWidth(delete.getWidth() - 6);
            }

            AbstractWidget direct = ScreenHelpers.findButton(I18n.get("selectServer.direct"), abstractWidgets);
            if (direct != null)
            {
                direct.x = (multiplayerScreen.width / 2) - 23;
                direct.y = (multiplayerScreen.height - 28);
                direct.setWidth(direct.getWidth());
            }

            AbstractWidget cancel = ScreenHelpers.findButton(I18n.get("selectServer.cancel"), abstractWidgets);
            if (cancel != null)
            {
                cancel.x += 1;
                cancel.setWidth(cancel.getWidth() - 2);
            }

            if (!(multiplayerScreen instanceof JoinMultiplayerScreenPublic))
            {
                ScreenHooks.addRenderableWidget(multiplayerScreen, new Button(multiplayerScreen.width / 2 + 80, multiplayerScreen.height - 52, 74, 20, new TranslatableComponent("selectServer.refresh"), p -> Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()))));
            }
        } catch (Exception ignored)
        {
        }
    }
}
