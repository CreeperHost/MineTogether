package net.creeperhost.minetogether.chat;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.chat.gui.ChatScreen;
import net.creeperhost.minetogether.chat.ingame.MTChatComponent;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.MutedUserList;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.irc.pircbotx.PircBotClient;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.request.IRCServerListRequest;
import net.creeperhost.minetogether.lib.chat.request.IRCServerListResponse;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.polylib.gui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
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
    private static final MutedUserList MUTED_USER_LIST = new MutedUserList(
            Platform.getGameFolder().resolve("local/minetogether/mutedusers.json")
    );

    @Nullable // TODO make this nonnull, when api server list api request is moved into irc client init.
    private static IrcClient ircClient;

    public static ChatComponent vanillaChat;
    public static MTChatComponent publicChat;
    public static ChatTarget target = ChatTarget.PUBLIC;

    public static void init() {
        // Class Initializer must be finished before MTChatComponent is constructed.
        publicChat = new MTChatComponent(ChatTarget.PUBLIC, Minecraft.getInstance());

        ClientGuiEvent.INIT_POST.register(MineTogetherChat::onScreenOpen);

        if (Config.instance().debugMode) {
            System.setProperty("net.covers1624.pircbot.logging.info", "INFO");
            System.setProperty("net.covers1624.pircbot.logging.debug", "INFO");
            System.setProperty("net.covers1624.pircbot.logging.very_verbose", "true");
        }
    }

    public static void initChat(Gui gui) {
        vanillaChat = gui.chat;
        publicChat = new MTChatComponent(ChatTarget.PUBLIC, Minecraft.getInstance());

        // TODO throw this off thread inside the IRC client initialization.
        try {
            ApiClientResponse<IRCServerListResponse> response = API.execute(new IRCServerListRequest());
            ircClient = new PircBotClient(CHAT_AUTH, MUTED_USER_LIST, API, response.apiResponse(), "{\"p\":\"-1\"}");
            ircClient.connect();

            ircClient.addChannelListener(new IrcClient.ChannelListener() {
                @Override
                public void channelJoin(IrcChannel channel) {
                    if (ircClient.getPrimaryChannel() == null) return;
                    if (channel == ircClient.getPrimaryChannel()) {
                        publicChat.attach(channel);
                    }
                }

                @Override
                public void channelLeave(IrcChannel channel) {

                }
            });
        } catch (IOException ex) {
            LOGGER.error("Failed to initialize IRC client.", ex);
        }
    }

    public static IrcClient getIrcClient() {
        return Objects.requireNonNull(ircClient, "IRC client could not be constructed.");
    }

    public static Profile getOurProfile() {
        return getIrcClient().getUserProfile();
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (screen instanceof TitleScreen) {
            ScreenHooks.addRenderableWidget(screen, new IconButton(screen.width - 25, 5, 1, MineTogetherClient.WIDGETS_SHEET, e -> {
                Minecraft.getInstance().setScreen(new ChatScreen(screen));
            }));
        }
    }
}
