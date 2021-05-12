package net.creeperhost.minetogether;

import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.hooks.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.helpers.ScreenHelpers;
import net.creeperhost.minetogetherlib.Order;
import net.creeperhost.minetogether.module.serverorder.screen.OrderServerScreen;
import net.creeperhost.minetogether.screen.ChatScreen;
import net.creeperhost.minetogether.screen.FriendsListScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogether.screen.widgets.ButtonMultiple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class MineTogetherClient
{
    public static ToastHandler toastHandler;
    public static void init()
    {
        GuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
    {
        if (screen instanceof TitleScreen)
        {
            AbstractWidget relms = ScreenHelpers.removeButton("menu.online", abstractWidgets);
            if(relms != null)
            {
                ScreenHooks.addButton(screen, new Button(relms.x, relms.y, relms.getWidth(), relms.getHeight(), new TranslatableComponent("minetogether.button.getserver"), p ->
                {
                    Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order()));
                }));
            }

            ScreenHooks.addButton(screen, new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether.multiplayer.friends"), p ->
            {
                Minecraft.getInstance().setScreen(new FriendsListScreen(screen));
            }));

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
