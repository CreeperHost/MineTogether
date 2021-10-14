package net.creeperhost.minetogether.compat.kubejs;

import net.creeperhost.minetogether.lib.Order;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.minetogether.module.serverorder.screen.OrderServerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;

public interface KubeJSUIIntegration
{
    Consumer<Screen> CHAT = screen -> Minecraft.getInstance().setScreen(new ChatScreen(screen));
    Consumer<Screen> FRIENDS_LIST = screen -> Minecraft.getInstance().setScreen(new FriendsListScreen(screen));
    Consumer<Screen> ORDER = screen -> Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), screen));
}
