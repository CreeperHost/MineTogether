package net.creeperhost.minetogether.module.multiplayer;

import me.shedaniel.architectury.hooks.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.mixin.MixinMultiplayerScreen;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.multiplayer.data.CreeperHostServerEntry;
import net.creeperhost.minetogether.module.multiplayer.screen.JoinMultiplayerScreenPublic;
import net.creeperhost.minetogether.module.multiplayer.screen.ServerTypeScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiPlayerModule
{
    public static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
    {
        if(screen instanceof JoinMultiplayerScreen)
        {
            JoinMultiplayerScreen multiplayerScreen = (JoinMultiplayerScreen) screen;
            //Add our entry into the server list
            addCreeperHostServerEntry(multiplayerScreen);
            //Clean up the buttons in the screen to allow us to add ours
            updateMultiPlayerScreenButtons(multiplayerScreen, abstractWidgets);

            //Don't add the serverlist button if the multiplayer screen is ours
            if(!(screen instanceof JoinMultiplayerScreenPublic))
            {
                ScreenHooks.addButton(screen, new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether.multiplayer.serverlist"),
                        p -> Minecraft.getInstance().setScreen(new ServerTypeScreen(screen))));

                ScreenHooks.addButton(screen, new ButtonMultiple(screen.width - 125, 5, 1, p ->
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
            ScreenHelpers.findButton(I18n.get("selectServer.add"), abstractWidgets).x -= 23;
            ScreenHelpers.removeButton(I18n.get("selectServer.refresh"), abstractWidgets);

            AbstractWidget edit = ScreenHelpers.findButton(I18n.get("selectServer.edit"), abstractWidgets);
            if (edit != null) {
                edit.setWidth(edit.getWidth() - 9);
                edit.x += 1;
            }

            AbstractWidget delete = ScreenHelpers.findButton(I18n.get("selectServer.delete"), abstractWidgets);
            if (delete != null) {
                delete.x -= 16;
                delete.setWidth(delete.getWidth() - 6);
            }

            AbstractWidget direct = ScreenHelpers.findButton(I18n.get("selectServer.direct"), abstractWidgets);
            if (direct != null) {
                direct.x = (multiplayerScreen.width / 2) - 23;
                direct.y = (multiplayerScreen.height - 28);
                direct.setWidth(direct.getWidth());
            }

            AbstractWidget cancel = ScreenHelpers.findButton(I18n.get("selectServer.cancel"), abstractWidgets);
            if (cancel != null) {
                cancel.x += 1;
                cancel.setWidth(cancel.getWidth() - 2);
            }

            ScreenHooks.addButton(multiplayerScreen, new ButtonMultiple(multiplayerScreen.width / 2 + 134, multiplayerScreen.height - 52, 2, p ->
            {
                Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
            }));

            ScreenHooks.addButton(multiplayerScreen, new Button(multiplayerScreen.width / 2 - 50, multiplayerScreen.height - 52, 78, 20, new TranslatableComponent("minetogether.button.minigames"), p ->
            {
                //TODO Once the Minigames Screen has been added
//            Minecraft.getInstance().setScreen(new MinigamesScreen(event.getGui()));
            }));
            //Another mod has messed with a button, Lets not crash and continue
        } catch (Exception ignored) {}
    }

    public static void addCreeperHostServerEntry(JoinMultiplayerScreen multiplayerScreen)
    {
        //Don't add our entry on a the public server list screen
        if(multiplayerScreen instanceof JoinMultiplayerScreenPublic) return;

        ServerSelectionList serverSelectionList = ((MixinMultiplayerScreen) multiplayerScreen).getServerSelectionList();
        CreeperHostServerEntry creeperHostEntry = new CreeperHostServerEntry(multiplayerScreen, null, serverSelectionList);

        //Check to see if we already have an entry in the list before adding it
        AtomicBoolean hasEntry = new AtomicBoolean(false);
        serverSelectionList.children().forEach(entry ->
        {
            if(entry instanceof CreeperHostServerEntry) hasEntry.set(true);
        });
        if(Config.getInstance().isMpMenuEnabled() && !hasEntry.get()) serverSelectionList.children().add(0, creeperHostEntry);
    }
}
