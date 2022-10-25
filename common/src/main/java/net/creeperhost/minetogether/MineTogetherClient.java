package net.creeperhost.minetogether;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.connect.MineTogetherConnect;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.orderform.OrderForm;
import net.creeperhost.minetogether.serverlist.MineTogetherServerList;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.serverlist.web.GetServerRequest;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

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

        MineTogetherChat.init();
        MineTogetherServerList.init();
        OrderForm.init();
        MineTogetherConnect.init();

        ClientGuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
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
            ConnectScreen.startConnecting(new JoinMultiplayerScreen(screen), Minecraft.getInstance(), ServerAddress.parseString(serverData.ip), serverData);
        }
    }
}
