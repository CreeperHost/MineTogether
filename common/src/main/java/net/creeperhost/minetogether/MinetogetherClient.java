package net.creeperhost.minetogether;

import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.hooks.ScreenHooks;
import net.creeperhost.minetogether.screen.ChatScreen;
import net.creeperhost.minetogether.screen.widgets.ButtonMultiple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class MinetogetherClient
{
    public static void init()
    {
        GuiEvent.INIT_POST.register(MinetogetherClient::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
    {
        if (screen instanceof TitleScreen)
        {
            ScreenHooks.addButton(screen, new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent(I18n.get("minetogether.multiplayer.friends")), p ->
            {
                //TODO
//                MineTogether.proxy.openFriendsGui();
            }));

            ScreenHooks.addButton(screen, new ButtonMultiple(screen.width - 125, 5, 1, p ->
            {
                //TODO
//                if (Config.getInstance().isChatEnabled()) {
                    Minecraft.getInstance().setScreen(new ChatScreen(screen));
//                } else {
//                    Minecraft.getInstance().displayGuiScreen(new SettingsScreen(event.getGui()));
//                }
            }));
        }
    }
}
