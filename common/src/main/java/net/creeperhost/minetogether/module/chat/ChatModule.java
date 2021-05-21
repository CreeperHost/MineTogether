package net.creeperhost.minetogether.module.chat;

import me.shedaniel.architectury.hooks.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ChatModule
{
    public static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
    {
        if (screen instanceof TitleScreen)
        {
            if(Config.getInstance().isEnableMainMenuFriends())
            {
                ScreenHooks.addButton(screen, new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether.multiplayer.friends"), p ->
                        Minecraft.getInstance().setScreen(new FriendsListScreen(screen))));

                ScreenHooks.addButton(screen, new ButtonMultiple(screen.width - 125, 5, 1, p ->
                {
                    if (Config.getInstance().isChatEnabled()) {
                        Minecraft.getInstance().setScreen(new ChatScreen(screen));
                    } else {
                        Minecraft.getInstance().setScreen(new SettingsScreen(screen));
                    }
                }));
            }
        }
    }
}
