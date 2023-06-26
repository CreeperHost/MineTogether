package net.creeperhost.minetogether.orderform;

import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.screen.OrderServerScreen;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

/**
 * Created by covers1624 on 26/10/22.
 */
public class OrderForm {



    public static void onScreenPostInit(Screen screen) {
        if (screen instanceof TitleScreen && Config.instance().replaceRealms) {
            AbstractWidget realms = ButtonHelper.removeButton("menu.online", screen);
            if (realms != null) {
                ScreenHooks.addRenderableWidget(screen, Button.builder(Component.translatable("minetogether:button.getserver"), p ->
                                Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), screen)))
                        .bounds(realms.getX(), realms.getY(), realms.getWidth(), realms.getHeight())
                        .build()
                );
            }
        }
    }
}
