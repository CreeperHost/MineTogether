package net.creeperhost.minetogether.chat;

import dev.architectury.hooks.client.screen.ScreenHooks;
import dev.architectury.platform.Platform;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.gui.ChatScreen;
import net.creeperhost.minetogether.chat.gui.FriendChatGui;
import net.creeperhost.minetogether.chat.gui.PublicChatGui;
import net.creeperhost.minetogether.chat.gui.SettingGui;
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
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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

    public static ChatState CHAT_STATE = new ChatState(API, CHAT_AUTH, MUTED_USER_LIST, () -> ModPackInfo.getInfo().realName, false);

    public static ChatComponent vanillaChat;
    public static MTChatComponent publicChat;
    private static boolean hasHitLoadingScreen = false;

    public static void init() {
        CHAT_STATE.logChatToConsole = Config.instance().logChatToConsole | Config.instance().debugMode;

        // Class Initializer must be finished before MTChatComponent is constructed.
        publicChat = new MTChatComponent(ChatTarget.PUBLIC, Minecraft.getInstance());

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

                    Screen screen = Minecraft.getInstance().screen;

                    // If we have the ChatScreen open. Attach to main chat.
                    if (screen instanceof ChatScreen chat) {
                        chat.attach(channel);
                    } else if (screen instanceof ModularGuiScreen mgui && mgui.getModularGui().getProvider() instanceof PublicChatGui chat) {
                        chat.chatMonitor.attach(channel);
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
                        Component.translatable("minetogether:toast.fiend_request_received", displayName(fr.from)),
                        Component.empty(),
                        MINETOGETHER_LOGO_25
                ));
            } else if (e.type == ProfileManager.EventType.FRIEND_REQUEST_ACCEPTED) {
                Profile fr = (Profile) e.data;
                addToast(new SimpleToast(
                        Component.translatable("minetogether:toast.fiend_request_accepted", displayName(fr)),
                        Component.empty(),
                        MINETOGETHER_LOGO_25
                ));
            } else if (e.type == ProfileManager.EventType.FRIEND_ONLINE && Config.instance().friendNotifications) {
                Profile fr = (Profile) e.data;
                addToast(new SimpleToast(
                        Component.translatable("minetogether:toast.user_online", displayName(fr)),
                        Component.empty(),
                        MINETOGETHER_LOGO_25
                ));
            } else if (e.type == ProfileManager.EventType.FRIEND_OFFLINE && Config.instance().friendNotifications) {
                Profile fr = (Profile) e.data;
                addToast(new SimpleToast(
                        Component.translatable("minetogether:toast.user_offline", displayName(fr)),
                        Component.empty(),
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

    public static void onScreenPostInit(Screen screen) {
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
        int buttonPos = 4;
        IconButton settings = new IconButton(screen.width - (buttonPos += 21), 5, 3, Constants.WIDGETS_SHEET, e -> Minecraft.getInstance().setScreen(new ModularGuiScreen(new SettingGui(), screen)));
        settings.setTooltip(Tooltip.create(Component.translatable("minetogether:gui.button.settings.info")));
        ScreenHooks.addRenderableWidget(screen, settings);

        IconButton friendChat = new IconButton(screen.width - (buttonPos += 21), 5, 7, Constants.WIDGETS_SHEET, e -> Minecraft.getInstance().setScreen(new ModularGuiScreen(new FriendChatGui(), screen)));
        friendChat.setTooltip(Tooltip.create(Component.translatable("minetogether:gui.button.friends.info")));
        ScreenHooks.addRenderableWidget(screen, friendChat);

        if (Config.instance().chatEnabled) {
            IconButton publicChat = new IconButton(screen.width - (buttonPos += 21), 5, 1, Constants.WIDGETS_SHEET, e -> Minecraft.getInstance().setScreen(new ModularGuiScreen(PublicChatGui.createGui(), screen)));
            publicChat.setTooltip(Tooltip.create(Component.translatable("minetogether:gui.button.global_chat.info")));
            ScreenHooks.addRenderableWidget(screen, publicChat);
        }
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
        CHAT_STATE.ircClient.stop();
    }

    public static void enableChat() {
        CHAT_STATE.ircClient.start();
    }

    public static void setTarget(ChatTarget target) {
        Config.instance().selectedTab = target;
        Config.save();
    }

    public static ChatTarget getTarget() {
        return Config.instance().chatEnabled ? Config.instance().selectedTab : ChatTarget.VANILLA;
    }

    public static String displayName(@Nullable Profile profile) {
        return profile == null ? "" : profile.isFriend() && profile.hasFriendName() ? profile.getFriendName() : profile.getDisplayName();
    }
}
