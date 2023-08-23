package net.creeperhost.minetogether.orderform;

import me.shedaniel.architectury.event.events.GuiEvent;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.screen.OrderServerScreen;
import net.creeperhost.minetogether.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

/**
 * Created by covers1624 on 26/10/22.
 */
public class OrderForm {

    public static void init() {
        GuiEvent.INIT_POST.register(OrderForm::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, List<AbstractWidget> widgets, List<GuiEventListener> children) {
        if (screen instanceof TitleScreen && Config.instance().replaceRealms) {
            AbstractWidget realms = ButtonHelper.removeButton("menu.online", screen);
            if (realms != null) {
                screen.addButton(new Button(realms.x, realms.y, realms.getWidth(), realms.getHeight(), new TranslatableComponent("minetogether:button.getserver"), p ->
                        Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), screen)))
                );
            }
        }
    }
}
