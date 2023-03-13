package net.creeperhost.minetogether.serverlist;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.chat.gui.ChatScreen;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.gui.SettingsScreen;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.polylib.gui.IconButton;
import net.creeperhost.minetogether.serverlist.data.ListType;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.serverlist.gui.JoinMultiplayerScreenPublic;
import net.creeperhost.minetogether.serverlist.gui.ServerTypeScreen;
import net.creeperhost.minetogether.serverlist.web.GetServerListRequest;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 25/10/22.
 */
public class MineTogetherServerList {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<Server> servers = new LinkedList<>();
    @Nullable
    private static ListType lastRequest;
    private static long lastRequestTime = 0;
    private static boolean incorrectlyConfigured = false;

    public static void init() {
        ModPackInfo.waitForInfo(versionInfo -> {
            incorrectlyConfigured = versionInfo.curseID.isEmpty();
            if (incorrectlyConfigured) {
                Server server = new Server();
                server.name = "No project ID! Please fix the MineTogether config or ensure a version.json exists.";
                synchronized (servers) {
                    servers.add(server);
                }
            }
        });

        ClientGuiEvent.INIT_POST.register(MineTogetherServerList::onScreenOpen);
    }

    public static List<Server> updateServers(ListType type) {
        synchronized (servers) {
            if (incorrectlyConfigured || lastRequest == type && lastRequestTime + 30000 < System.currentTimeMillis()) return Collections.unmodifiableList(servers);

            try {
                ApiClientResponse<GetServerListRequest.Response> resp = MineTogether.API.execute(new GetServerListRequest(type, MineTogetherChat.CHAT_AUTH.getHash()));
                servers.clear();
                servers.addAll(resp.apiResponse().servers);
            } catch (Throwable ex) {
                LOGGER.error("Failed to update server listings.", ex);
            }
            return Collections.unmodifiableList(servers);
        }
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (!(screen instanceof JoinMultiplayerScreen mpScreen)) return;
        if (screen instanceof JoinMultiplayerScreenPublic) return;

        Button serverListButton = new Button(screen.width - 105, 5, 100, 20, Component.translatable("minetogether:screen.multiplayer.serverlist"), e -> {
            Minecraft.getInstance().setScreen(new ServerTypeScreen(mpScreen));
        });
        serverListButton.active = !MineTogetherChat.isNewUser();

        ScreenHooks.addRenderableWidget(mpScreen, serverListButton);

        boolean chatEnabled = Config.instance().chatEnabled;
        ScreenHooks.addRenderableWidget(screen, new IconButton(screen.width - 125, 5, chatEnabled ? 1 : 3, Constants.WIDGETS_SHEET, e -> {
            Minecraft.getInstance().setScreen(chatEnabled ? new ChatScreen(screen) : new SettingsScreen(screen));
        }));
    }
}
