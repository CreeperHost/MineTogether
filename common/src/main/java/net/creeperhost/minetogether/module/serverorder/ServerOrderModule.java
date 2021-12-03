package net.creeperhost.minetogether.module.serverorder;

import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.Order;
import net.creeperhost.minetogether.module.serverorder.screen.OrderServerScreen;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ServerOrderModule
{
    public static void onScreenOpen(Screen screen, ScreenAccess screenAccess)
    {
        if (screen instanceof TitleScreen)
        {
            if (Config.getInstance().getReplaceRealms())
            {
                AbstractWidget relms = ScreenHelpers.removeButton("menu.online", screen);
                if (relms != null)
                {
                    ScreenHooks.addRenderableWidget(screen, new Button(relms.x, relms.y, relms.getWidth(), relms.getHeight(), new TranslatableComponent("minetogether.button.getserver"), p -> Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), screen))));
                }
            }
        }
    }
}
