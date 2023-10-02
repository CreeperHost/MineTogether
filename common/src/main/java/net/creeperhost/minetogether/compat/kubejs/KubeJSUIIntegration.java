package net.creeperhost.minetogether.compat.kubejs;

import net.creeperhost.minetogether.chat.gui.FriendsListScreen;
import net.creeperhost.minetogether.chat.gui.PublicChatGui;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.screen.OrderServerScreen;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;

public interface KubeJSUIIntegration {

    Consumer<Screen> CHAT = screen -> Minecraft.getInstance().setScreen(new ModularGuiScreen(PublicChatGui.createGui(), screen));
    Consumer<Screen> FRIENDS_LIST = screen -> Minecraft.getInstance().setScreen(new FriendsListScreen(screen));
    Consumer<Screen> ORDER = screen -> Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), screen));
}
