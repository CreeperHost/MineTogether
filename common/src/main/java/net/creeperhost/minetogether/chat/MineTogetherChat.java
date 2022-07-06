package net.creeperhost.minetogether.chat;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.chat.gui.ChatScreen;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.PircBotClient;
import net.creeperhost.minetogether.lib.chat.request.IRCServerListRequest;
import net.creeperhost.minetogether.lib.chat.request.IRCServerListResponse;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.polylib.gui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

import static net.creeperhost.minetogether.MineTogether.API;

/**
 * @author covers1624
 */
public class MineTogetherChat {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final ChatAuthImpl CHAT_AUTH = new ChatAuthImpl(Minecraft.getInstance());

    @Nullable // TODO make this nonnull, when api server list api request is moved into irc client init.
    private static IrcClient ircClient;

    public static void init() {
        ClientGuiEvent.INIT_POST.register(MineTogetherChat::onScreenOpen);

        if (Config.instance().debugMode) {
            System.setProperty("net.covers1624.pircbot.logging.info", "INFO");
            System.setProperty("net.covers1624.pircbot.logging.debug", "INFO");
            System.setProperty("net.covers1624.pircbot.logging.very_verbose", "true");
        }

        // TODO throw this off thread inside the IRC client initialization.
        try {
            ApiClientResponse<IRCServerListResponse> response = API.execute(new IRCServerListRequest());
            ircClient = new PircBotClient(CHAT_AUTH, API, response.apiResponse(), "{\"p\":\"-1\"}");
            ircClient.connect();
        } catch (IOException ex) {
            LOGGER.error("Failed to initialize IRC client.", ex);
        }
    }

    public static IrcClient getIrcClient() {
        return Objects.requireNonNull(ircClient, "IRC client could not be constructed.");
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (screen instanceof TitleScreen) {
            ScreenHooks.addRenderableWidget(screen, new IconButton(screen.width - 25, 5, 1, MineTogetherClient.WIDGETS_SHEET, e -> {
                Minecraft.getInstance().setScreen(new ChatScreen(screen));
            }));
        }
    }
}
