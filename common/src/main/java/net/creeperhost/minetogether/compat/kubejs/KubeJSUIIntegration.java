package net.creeperhost.minetogether.compat.kubejs;

import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.chat.gui.ChatScreen;
import net.creeperhost.minetogether.chat.gui.FriendsListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;

public interface KubeJSUIIntegration {

    Consumer<Screen> CHAT = screen -> Minecraft.getInstance().setScreen(new ChatScreen(screen));
    Consumer<Screen> FRIENDS_LIST = screen -> Minecraft.getInstance().setScreen(new FriendsListScreen(screen));
    Consumer<Screen> ORDER = screen -> Minecraft.getInstance().setScreen(MineTogetherClient.orderScreen());
}
