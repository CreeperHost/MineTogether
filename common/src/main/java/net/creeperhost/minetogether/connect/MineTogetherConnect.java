package net.creeperhost.minetogether.connect;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MineTogetherConnect {

    public static Executor connectExecutor = Executors.newSingleThreadExecutor();
    public static boolean isInitted = false;

    public static void init() {
        CompletableFuture.runAsync(ConnectHandler::probeEnabled, connectExecutor);
        isInitted = true;
        Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("minetogether-connect-%d").build());
        ClientGuiEvent.INIT_POST.register(MineTogetherConnect::onScreenOpen);
        LifecycleEvent.SERVER_STOPPING.register(MineTogetherConnect::onServerStopping);
    }

    public static void onServerStopping(MinecraftServer server) {
        if (ConnectHelper.isShared(server)) {
            ConnectHandler.close();
        }
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (!(screen instanceof PauseScreen)) return;

        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if (integratedServer == null) return;

        @SuppressWarnings ("unchecked")
        List<GuiEventListener> children = (List<GuiEventListener>) screen.children();
        List<Widget> renderables = screenAccess.getRenderables();
        List<NarratableEntry> narratables = screenAccess.getNarratables();

        // Replace bugs button with our own button.
        //TODO: move this to somewhere else, as not really relevant to the connect module
        AbstractWidget bugs = ButtonHelper.findButton("menu.reportBugs", screen);
        if (bugs != null && Config.instance().issueTrackerUrl != null) {
            Button ourBugsButton = new Button(bugs.x, bugs.y, bugs.getWidth(), bugs.getHeight(), Component.translatable("menu.reportBugs"), (button) -> {
                String s = Config.instance().issueTrackerUrl;
                Minecraft.getInstance().setScreen(new ConfirmLinkScreen((p_213069_2_) -> {
                    if (p_213069_2_) {
                        Util.getPlatform().openUri(s);
                    }

                    Minecraft.getInstance().setScreen(screen);
                }, s, true));
            });
            // We have to keep these indexes the same and remove the old button due to how Mod Menu works...
            children.set(children.indexOf(bugs), ourBugsButton);
            renderables.set(renderables.indexOf(bugs), ourBugsButton);
            narratables.set(narratables.indexOf(bugs), ourBugsButton);
            bugs = ourBugsButton;
        }

        AbstractWidget feedBack = ButtonHelper.findButton("menu.sendFeedback", screen);
        AbstractWidget options = ButtonHelper.findButton("menu.options", screen);
        if (!Config.instance().moveButtonsOnPauseMenu || feedBack == null || options == null) {
            // Just add the button bellow the FriendsList button in the corner.
            // We either didn't find the Feedback and Options buttons, or moving these buttons was disabled in our config.
            Button guiButton = new Button(screen.width - 105, 25, 100, 20, Component.translatable("minetogether.connect.open"), (button) -> Minecraft.getInstance().setScreen(new GuiShareToFriends(screen)));
            ScreenHooks.addRenderableWidget(screen, guiButton);
            return;
        }

        // Open To Friends button goes where the options button was.
        Button guiButton = new Button(options.x, options.y, 98, 20, Component.translatable("minetogether.connect.open"), (button) -> Minecraft.getInstance().setScreen(new GuiShareToFriends(screen)));

        guiButton.active = !integratedServer.isPublished();
        ScreenHooks.addRenderableWidget(screen, guiButton);

        // Move the options button to where the feedback button was.
        options.y = feedBack.y;
        options.x = feedBack.x;

        // Again, we have to juggle indexes because of Mod Menu...
        children.remove(options);
        renderables.remove(options);
        narratables.remove(options);
        children.set(children.indexOf(feedBack), options);
        renderables.set(renderables.indexOf(feedBack), options);
        narratables.set(narratables.indexOf(feedBack), options);
    }
}
