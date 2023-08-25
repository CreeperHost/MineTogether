package net.creeperhost.minetogether.connect;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.connect.gui.GuiShareToFriends;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MineTogetherConnect {

    public static boolean isInitted = false;

    public static void init() {
        isInitted = true;
        ConnectHandler.init();

        ClientGuiEvent.INIT_POST.register(MineTogetherConnect::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (!(screen instanceof PauseScreen)) return;

        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if (integratedServer == null) return;

        boolean isConnectPublished = ConnectHandler.isPublished();
        Component buttonText = isConnectPublished ? Component.translatable("minetogether.connect.close") : Component.translatable("minetogether.connect.open");
        Button.OnPress action = button -> {
            if (isConnectPublished) {
                ConnectHandler.unPublish();
                Minecraft.getInstance().setScreen(new PauseScreen(true));
            } else {
                Minecraft.getInstance().setScreen(new GuiShareToFriends(screen));
            }
        };

        @SuppressWarnings ("unchecked")
        List<GuiEventListener> children = (List<GuiEventListener>) screen.children();
        List<Widget> renderables = screenAccess.getRenderables();
        List<NarratableEntry> narratables = screenAccess.getNarratables();

        AbstractWidget feedBack = ButtonHelper.findButton("menu.sendFeedback", screen);
        AbstractWidget options = ButtonHelper.findButton("menu.options", screen);
        if (!Config.instance().moveButtonsOnPauseMenu || feedBack == null || options == null) {
            // Just add the button bellow the FriendsList button in the corner.
            // We either didn't find the Feedback and Options buttons, or moving these buttons was disabled in our config.
            Button openToFriends = new Button(screen.width - 105, 25, 100, 20, buttonText, action);
            ScreenHooks.addRenderableWidget(screen, openToFriends);
            return;
        }

        // Open To Friends button goes where the options button was.
        Button openToFriends = new Button(options.x, options.y, 98, 20, buttonText, action);
        ScreenHooks.addRenderableWidget(screen, openToFriends);

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
