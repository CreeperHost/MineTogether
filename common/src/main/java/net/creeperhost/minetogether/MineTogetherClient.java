package net.creeperhost.minetogether;

import me.shedaniel.architectury.event.events.GuiEvent;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.connect.MineTogetherConnect;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.orderform.OrderForm;
import net.creeperhost.minetogether.polylib.client.screen.ButtonHelper;
import net.creeperhost.minetogether.serverlist.MineTogetherServerList;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.serverlist.web.GetServerRequest;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.creeperhost.minetogether.util.MTSessionProvider;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Initialize on a client.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherClient {

    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean first = true;

    public static void init() {
        LOGGER.info("Initializing MineTogetherClient!");

        MineTogetherSession.getDefault().setProvider(new MTSessionProvider());
        // Trigger session validation early in the background.
        MineTogetherSession.getDefault().getTokenAsync();

        MineTogetherChat.init();
        MineTogetherServerList.init();
        OrderForm.init();
        MineTogetherConnect.init();

        GuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, List<AbstractWidget> widgets, List<GuiEventListener> children) {
        if (screen instanceof TitleScreen && first) {
            first = false;
            String serverProp = System.getProperty("mt.server");
            if (serverProp == null) return;

            Server server;
            try {
                ApiClientResponse<GetServerRequest.Response> resp = MineTogether.API.execute(new GetServerRequest(serverProp));
                if (resp.apiResponse().getStatus().equals("error")) {
                    LOGGER.error("Failed to load server with id: {}. Message: {}", serverProp, resp.apiResponse().getMessageOrNull());
                    return;
                }
                server = resp.apiResponse().server;
                if (server == null) {
                    LOGGER.error("Returned empty server?");
                    return;
                }
            } catch (IOException ex) {
                LOGGER.error("Failed to query server.", ex);
                return;
            }

            ServerData serverData = new ServerData(server.ip, String.valueOf(server.port), false);
            Minecraft.getInstance().setScreen(new ConnectScreen(new JoinMultiplayerScreen(screen), Minecraft.getInstance(), serverData));
        } else if (screen instanceof PauseScreen) {
            // Replace bugs button with our own button.
            AbstractWidget bugs = ButtonHelper.findButton("menu.reportBugs", screen);
            if (bugs != null && Config.instance().issueTrackerUrl != null) {
                Button ourBugsButton = new Button(bugs.x, bugs.y, bugs.getWidth(), bugs.getHeight(), new TranslatableComponent("menu.reportBugs"), (button) -> {
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
                widgets.set(widgets.indexOf(bugs), ourBugsButton);
                bugs = ourBugsButton;
            }
        }
    }
}
