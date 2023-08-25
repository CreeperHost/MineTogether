package net.creeperhost.minetogether.connect;

import me.shedaniel.architectury.event.events.GuiEvent;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.connect.gui.GuiShareToFriends;
import net.creeperhost.minetogether.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class MineTogetherConnect {

    public static boolean isInitted = false;

    public static void init() {
        isInitted = true;
        ConnectHandler.init();

        GuiEvent.INIT_POST.register(MineTogetherConnect::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, List<AbstractWidget> renderables, List<GuiEventListener> children) {
        if (!(screen instanceof PauseScreen)) return;

        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if (integratedServer == null) return;

        boolean isConnectPublished = ConnectHandler.isPublished();
        Component buttonText = isConnectPublished ? new TranslatableComponent("minetogether.connect.close") : new TranslatableComponent("minetogether.connect.open");
        Button.OnPress action = button -> {
            if (isConnectPublished) {
                ConnectHandler.unPublish();
                Minecraft.getInstance().setScreen(new PauseScreen(true));
            } else {
                Minecraft.getInstance().setScreen(new GuiShareToFriends(screen));
            }
        };

        AbstractWidget feedBack = ButtonHelper.findButton("menu.sendFeedback", screen);
        AbstractWidget options = ButtonHelper.findButton("menu.options", screen);
        if (!Config.instance().moveButtonsOnPauseMenu || feedBack == null || options == null) {
            // Just add the button bellow the FriendsList button in the corner.
            // We either didn't find the Feedback and Options buttons, or moving these buttons was disabled in our config.
            Button openToFriends = new Button(screen.width - 105, 25, 100, 20, buttonText, action);
            screen.addButton(openToFriends);
            return;
        }

        // Open To Friends button goes where the options button was.
        Button openToFriends = new Button(options.x, options.y, 98, 20, buttonText, action);
        screen.addButton(openToFriends);

        // Move the options button to where the feedback button was.
        options.y = feedBack.y;
        options.x = feedBack.x;

        // Again, we have to juggle indexes because of Mod Menu...
        children.remove(options);
        renderables.remove(options);
        children.set(children.indexOf(feedBack), options);
        renderables.set(renderables.indexOf(feedBack), options);
    }
}
