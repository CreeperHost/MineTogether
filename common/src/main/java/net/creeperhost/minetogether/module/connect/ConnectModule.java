package net.creeperhost.minetogether.module.connect;

import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.LifecycleEvent;
import me.shedaniel.architectury.hooks.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class ConnectModule {
    public static void init() {
        GuiEvent.INIT_POST.register(ConnectModule::onScreenOpen);
        LifecycleEvent.SERVER_STOPPING.register(ConnectModule::onServerStopping);
    }

    private static void onServerStopping(MinecraftServer server) {
        if(server instanceof IntegratedServer) {
            if(ConnectHelper.isShared((IntegratedServer)server)) {
                ConnectHandler.close();
            }
        }
    }

    private static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners) {
        if (screen instanceof PauseScreen) {
            IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
            if (integratedServer != null) {
                AbstractWidget feedBack = ScreenHelpers.removeButton("menu.sendFeedback", abstractWidgets);
                AbstractWidget bugs = ScreenHelpers.removeButton("menu.reportBugs", abstractWidgets);
                AbstractWidget openToLan = ScreenHelpers.findButton("menu.shareToLan", abstractWidgets);
                AbstractWidget options = ScreenHelpers.findButton("menu.options", abstractWidgets);

                if(openToLan != null && feedBack != null)
                {
                    openToLan.y = feedBack.y;
                }

                Button guiButton = new Button(screen.width / 2 - 100, screen.height / 4 + 72 + -16, 98, 20, new TranslatableComponent("minetogether.connect.open"), (button) -> Minecraft.getInstance().setScreen(new GuiShareToFriends(screen)));

                guiButton.active = ConnectHelper.isEnabled && !integratedServer.isPublished();
                ScreenHooks.addButton(screen, guiButton);

                if(bugs == null || feedBack == null) return;

                //TODO: move this to somewhere else, as not really relevant to the connect module
                Button ourFeedback = new Button(bugs.x, options.y, feedBack.getWidth(), 20, new TranslatableComponent("menu.reportBugs"), (button) ->
                {
                    String s = Config.getInstance().getIssueTrackerUrl();
                    Minecraft.getInstance().setScreen(new ConfirmLinkScreen((p_213069_2_) -> {
                        if (p_213069_2_) {
                            Util.getPlatform().openUri(s);
                        }

                        Minecraft.getInstance().setScreen(screen);
                    }, s, true));
                });
                ScreenHooks.addButton(screen, ourFeedback);
            }
        }
    }
}
