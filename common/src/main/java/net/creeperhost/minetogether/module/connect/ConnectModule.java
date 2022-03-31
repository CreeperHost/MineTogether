package net.creeperhost.minetogether.module.connect;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConnectModule
{

    public static Executor connectExecutor = Executors.newSingleThreadExecutor();
    public static boolean isInitted = false;

    public static void init()
    {
        CompletableFuture.runAsync(ConnectHandler::connectToProc, connectExecutor);
        isInitted = true;
        Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("minetogether-connect-%d").build());
        ClientGuiEvent.INIT_POST.register(ConnectModule::onScreenOpen);
        LifecycleEvent.SERVER_STOPPING.register(ConnectModule::onServerStopping);
    }

    public static void onServerStopping(MinecraftServer server)
    {
        if (ConnectHelper.isShared(server))
        {
            ConnectHandler.close();
        }
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess)
    {
        if (screen instanceof PauseScreen)
        {
            IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
            if (integratedServer != null)
            {
                AbstractWidget feedBack = ButtonHelper.removeButton("menu.sendFeedback", screen);
                AbstractWidget bugs = ButtonHelper.removeButton("menu.reportBugs", screen);
                AbstractWidget openToLan = ButtonHelper.findButton("menu.shareToLan", screen);
                AbstractWidget options = ButtonHelper.findButton("menu.options", screen);

                if (openToLan != null && feedBack != null)
                {
                    openToLan.y = feedBack.y;
                }

                Button guiButton = new Button(screen.width / 2 - 100, screen.height / 4 + 72 + -16, 98, 20, new TranslatableComponent("minetogether.connect.open"), (button) -> Minecraft.getInstance().setScreen(new GuiShareToFriends(screen)));

                guiButton.active = !integratedServer.isPublished();
                ScreenHooks.addRenderableWidget(screen, guiButton);

                if (bugs == null || feedBack == null) return;

                //TODO: move this to somewhere else, as not really relevant to the connect module
                Button ourFeedback = new Button(bugs.x, options.y, feedBack.getWidth(), 20, new TranslatableComponent("menu.reportBugs"), (button) ->
                {
                    String s = Config.getInstance().getIssueTrackerUrl();
                    Minecraft.getInstance().setScreen(new ConfirmLinkScreen((p_213069_2_) ->
                    {
                        if (p_213069_2_)
                        {
                            Util.getPlatform().openUri(s);
                        }

                        Minecraft.getInstance().setScreen(screen);
                    }, s, true));
                });
                ScreenHooks.addRenderableWidget(screen, ourFeedback);
            }
        }
    }
}
