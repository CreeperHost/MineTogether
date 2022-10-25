package net.creeperhost.minetogether.orderform;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
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
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Created by covers1624 on 26/10/22.
 */
public class OrderForm {

    public static void init() {
        ClientGuiEvent.INIT_POST.register(OrderForm::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (screen instanceof TitleScreen && Config.instance().replaceRealms) {
            AbstractWidget realms = ButtonHelper.removeButton("menu.online", screen);
            if (realms != null) {
                ScreenHooks.addRenderableWidget(screen, new Button(realms.x, realms.y, realms.getWidth(), realms.getHeight(), new TranslatableComponent("minetogether:button.getserver"), p ->
                        Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), screen)))
                );
            }
        }
    }
}
