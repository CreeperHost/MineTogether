package net.creeperhost.minetogether.client.screen;

import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.order.GuiGetServer;
import net.creeperhost.minetogether.client.screen.serverlist.gui.FriendsListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public interface KubeJSUIIntegration
{
	Consumer<Screen> CHAT = screen -> Minecraft.getInstance().displayGuiScreen(new MTChatScreen(screen));
	Consumer<Screen> FRIENDS_LIST = screen -> Minecraft.getInstance().displayGuiScreen(new FriendsListScreen(screen));
	Consumer<Screen> ORDER = screen -> Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
}
