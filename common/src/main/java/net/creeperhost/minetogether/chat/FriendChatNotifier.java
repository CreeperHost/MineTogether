package net.creeperhost.minetogether.chat;

import dev.architectury.event.events.client.ClientTickEvent;
import net.covers1624.quack.collection.FastStream;
import net.creeperhost.minetogether.config.LocalConfig;
import net.creeperhost.minetogether.lib.chat.ChatState;
import net.creeperhost.minetogether.lib.chat.irc.IrcUser;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by brandon3055 on 06/07/2024
 */
public class FriendChatNotifier {
    private static int tick = 0;

    private static final Map<Profile, Integer> LAST_MESSAGE_COUNT = new HashMap<>();
    private static final Map<Profile, Integer> UNREAD_MESSAGES_COUNT = new HashMap<>();
    @Nullable
    private static Profile activeChat = null;

    public static void init() {
        ClientTickEvent.CLIENT_PRE.register(FriendChatNotifier::tick);
    }

    private static void tick(Minecraft mc) {
        if (tick++ % 20 == 0) return;

        ChatState chatState = MineTogetherChat.CHAT_STATE;
        ProfileManager profileManager = chatState.profileManager;
        Set<Profile> friends = FastStream.of(profileManager.getKnownProfiles())
                .filter(Profile::isFriend)
                .filter(Profile::isOnline)
                .toSet();

        LAST_MESSAGE_COUNT.entrySet().removeIf(e -> !e.getKey().isFriend());
        UNREAD_MESSAGES_COUNT.entrySet().removeIf(e -> !e.getKey().isFriend());

        for (Profile friend : friends) {
            IrcUser user = chatState.ircClient.getUser(friend);
            if (user == null) continue;

            int messageCount = user.getChannel().getMessages().size();
            int lastMessageCount = LAST_MESSAGE_COUNT.getOrDefault(friend, 0);

            if (messageCount > lastMessageCount) {
                int lastUnreadCount = UNREAD_MESSAGES_COUNT.getOrDefault(friend, 0);
                int newUnreadCount = lastUnreadCount + (messageCount - lastMessageCount);
                if (activeChat == friend) {
                    newUnreadCount = 0;
                }
                UNREAD_MESSAGES_COUNT.put(friend, newUnreadCount);
                LAST_MESSAGE_COUNT.put(friend, messageCount);
                if (newUnreadCount > 0) {
                    notifyMessageReceived(friend, newUnreadCount, lastUnreadCount);
                }
            }
        }
    }

    private static void notifyMessageReceived(Profile profile, int unreadCount, int lastUnreadCount) {
        if (lastUnreadCount == 0 && LocalConfig.instance().friendNotifications && activeChat == null) {
            String name = profile.hasFriendName() ? profile.getFriendName() : profile.getDisplayName();
            MineTogetherChat.simpleToast(new TranslatableComponent("minetogether:toast.friend_message", name));

            Component message = new TranslatableComponent("minetogether:chat.friend_message", new TextComponent(name).withStyle(ChatFormatting.GOLD))
                    .setStyle(Style.EMPTY
                            .applyFormat(ChatFormatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("minetogether:chat.friend_message.info")))
                            .withClickEvent(new OpenFriendEvent(profile))
                    );
            addNotificationMessage(message, profileToSig(profile));
        }
    }

    public static int getUnreadMessageCount(Profile profile) {
        return UNREAD_MESSAGES_COUNT.getOrDefault(profile, 0);
    }

    public static void resetUnreadMessageCount(Profile profile) {
        UNREAD_MESSAGES_COUNT.put(profile, 0);
    }

    public static void setActiveChat(@Nullable Profile activeChat) {
        FriendChatNotifier.activeChat = activeChat;
        if (activeChat != null) {
            resetUnreadMessageCount(activeChat);
        }
    }

    //TODO, This is now also used by blockshot, so it should be moved somewhere that makes more sense for general use.
    public static void addNotificationMessage(@Nullable Component message, int signature) {
        if (MineTogetherChat.getTarget() == ChatTarget.PUBLIC) {
            MineTogetherChat.publicChat.localMessage(message, signature);
        } else {
            Minecraft.getInstance().gui.getChat().removeById(signature);
            if (message != null) {
                MineTogetherChat.vanillaChat.addMessage(message, signature);
            }
        }
    }

    public static int profileToSig(Profile target) {
        return 0xC5000000 | (Objects.hash(target.getFullHash()) & 0x00FFFFFF);
    }

    //Just a custom event that can be intercepted by ChatScreenMixin
    public static class OpenFriendEvent extends ClickEvent {
        public final Profile profile;
        public OpenFriendEvent(Profile profile) {
            super(Action.RUN_COMMAND, "");
            this.profile = profile;
        }
    }
}
