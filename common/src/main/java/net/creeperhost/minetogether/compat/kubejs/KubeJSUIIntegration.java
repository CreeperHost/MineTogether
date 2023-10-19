package net.creeperhost.minetogether.compat.kubejs;

import net.creeperhost.minetogether.chat.gui.FriendChatGui;
import net.creeperhost.minetogether.chat.gui.PublicChatGui;
import net.creeperhost.minetogether.orderform.OrderGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;

public interface KubeJSUIIntegration {

    Consumer<Screen> CHAT = screen -> Minecraft.getInstance().setScreen(new ModularGuiScreen(PublicChatGui.createGui(), screen));
    Consumer<Screen> FRIENDS_LIST = screen -> Minecraft.getInstance().setScreen(new ModularGuiScreen(new FriendChatGui(), screen));
    Consumer<Screen> ORDER = screen -> Minecraft.getInstance().setScreen(new ModularGuiScreen(new OrderGui(), screen));
}
