package net.creeperhost.minetogether.chat;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.gui.ChatScreen;
import net.creeperhost.minetogether.chat.gui.FriendsListScreen;
import net.creeperhost.minetogether.chat.ingame.MTChatComponent;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.gui.SettingsScreen;
import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.MutedUserList;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.polylib.gui.IconButton;
import net.creeperhost.minetogether.polylib.gui.SimpleToast;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static net.creeperhost.minetogether.Constants.MINETOGETHER_LOGO_25;
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

    public static ChatState CHAT_STATE = new ChatState(API, CHAT_AUTH, MUTED_USER_LIST, ModPackInfo.realName, Config.instance().logChatToConsole | Config.instance().debugMode);

    public static ChatComponent vanillaChat;
    public static MTChatComponent publicChat;
    public static ChatTarget target = Config.instance().chatEnabled ? ChatTarget.PUBLIC : ChatTarget.VANILLA;
    private static boolean hasHitLoadingScreen = false;

    public static void init() {
        // Class Initializer must be finished before MTChatComponent is constructed.
        publicChat = new MTChatComponent(ChatTarget.PUBLIC, Minecraft.getInstance());

        ClientGuiEvent.INIT_POST.register(MineTogetherChat::onScreenOpen);

        if (Config.instance().debugMode) {
            System.setProperty("net.covers1624.pircbot.logging.info", "INFO");
            System.setProperty("net.covers1624.pircbot.logging.debug", "INFO");
            System.setProperty("net.covers1624.pircbot.logging.very_verbose", "true");
        }

        ChatStatistics.pollStats();
    }

    public static void initChat(Gui gui) {
        Minecraft mc = Minecraft.getInstance();
        vanillaChat = gui.chat;
        publicChat = new MTChatComponent(ChatTarget.PUBLIC, mc);
        if (Config.instance().chatEnabled) {
            CHAT_STATE.ircClient.start();
        }
        CHAT_STATE.ircClient.addChannelListener(new IrcClient.ChannelListener() {
            @Override
            public void channelJoin(IrcChannel channel) {
                if (CHAT_STATE.ircClient.getPrimaryChannel() == null) return;
                if (channel == CHAT_STATE.ircClient.getPrimaryChannel()) {
                    publicChat.attach(channel);

                    // If we have the ChatScreen open. Attach to main chat.
                    if (Minecraft.getInstance().screen instanceof ChatScreen chat) {
                        chat.attach(channel);
                    }
                }
            }

            @Override
            public void channelLeave(IrcChannel channel) {

            }
        });

        CHAT_STATE.profileManager.addListener(mc, (m, e) -> m.submit(() -> {
            if (e.type == ProfileManager.EventType.FRIEND_REQUEST_ADDED) {
                ProfileManager.FriendRequest fr = (ProfileManager.FriendRequest) e.data;
                addToast(new SimpleToast(
                        new TextComponent(fr.from.getDisplayName() + " has sent you a friend request"),
                        new TextComponent(" "),
                        MINETOGETHER_LOGO_25
                ));
            } else if (e.type == ProfileManager.EventType.FRIEND_REQUEST_ACCEPTED) {
                Profile fr = (Profile) e.data;
                addToast(new SimpleToast(
                        new TextComponent(fr.getDisplayName() + " has accepted your friend request"),
                        new TextComponent(" "),
                        MINETOGETHER_LOGO_25
                ));
            } else if (e.type == ProfileManager.EventType.FRIEND_ONLINE && Config.instance().friendNotifications) {
                Profile fr = (Profile) e.data;
                addToast(new SimpleToast(
                        new TextComponent(fr.getFriendName() + " Is now online."),
                        new TextComponent(" "),
                        MINETOGETHER_LOGO_25
                ));
            } else if (e.type == ProfileManager.EventType.FRIEND_OFFLINE && Config.instance().friendNotifications) {
                Profile fr = (Profile) e.data;
                addToast(new SimpleToast(
                        new TextComponent(fr.getFriendName() + " Is now offline."),
                        new TextComponent(" "),
                        MINETOGETHER_LOGO_25
                ));
            }
        }));

        // If the user has an account. Set firstConnect just incase.
        if (!Config.instance().firstConnect.equalsIgnoreCase(CHAT_AUTH.getHash()) && CHAT_STATE.profileManager.getOwnProfile().hasAccount()) {
            Config.instance().firstConnect = CHAT_AUTH.getHash();
            Config.save();
        }
    }

    private static void addToast(Toast toast) {
        if (hasHitLoadingScreen) {
            Minecraft.getInstance().getToasts().addToast(toast);
        } else {
            // YEET, too bad.
        }
    }

    public static Profile getOurProfile() {
        return CHAT_STATE.profileManager.getOwnProfile();
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (screen instanceof TitleScreen) {
            if (!hasHitLoadingScreen) {
                hasHitLoadingScreen = true;
            }
            if (Config.instance().mainMenuButtons) {
                addMenuButtons(screen);
            }
        } else if (screen instanceof PauseScreen) {
            if (Config.instance().pauseScreenButtons) {
                addMenuButtons(screen);
            }
        }
    }

    private static void addMenuButtons(Screen screen) {
        ScreenHooks.addRenderableWidget(screen, new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether:button.friends"), e -> {
            Minecraft.getInstance().setScreen(new FriendsListScreen(screen));
        }));
        boolean chatEnabled = Config.instance().chatEnabled;
        ScreenHooks.addRenderableWidget(screen, new IconButton(screen.width - 125, 5, chatEnabled ? 1 : 3, Constants.WIDGETS_SHEET, e -> {
            Minecraft.getInstance().setScreen(chatEnabled ? new ChatScreen(screen) : new SettingsScreen(screen));
        }));
    }

    public static boolean isNewUser() {
        if (MineTogetherChat.getOurProfile().hasAccount()) return false;

        return !Config.instance().firstConnect.equalsIgnoreCase(CHAT_AUTH.getHash());
    }

    public static void setNewUserResponded() {
        Config.instance().firstConnect = CHAT_AUTH.getHash();
        Config.save();
    }

    public static void disableChat() {
        target = ChatTarget.VANILLA;
        CHAT_STATE.ircClient.stop();
    }

    public static void enableChat() {
        CHAT_STATE.ircClient.start();
    }
}
