package net.creeperhost.minetogether.util;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.message.MessageComponent;
import net.creeperhost.minetogether.lib.chat.message.ProfileMessageComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import static net.minecraft.ChatFormatting.RESET;

/**
 * Created by covers1624 on 11/8/22.
 */
public class MessageFormatter {

    public static final String CLICK_NAME = "CE:CLICK_NAME";

    public static Component formatMessage(Message message) {
        ChatFormatting mc = getMessageColour(message);
        ChatFormatting ac = getArrowColour(message);
        ChatFormatting uc = getUserColour(message);

        // TODO obfuscate banned user messages.
        String sender = ac + "<" + uc + message.senderName + ac + ">" + RESET;
        String msg = formatMessage(message.getMessage(), mc);

        return new TextComponent(sender).withStyle(e -> e.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, CLICK_NAME))).append(" ").append(msg);
    }

    private static String formatMessage(MessageComponent comp, ChatFormatting messageColour) {
        StringBuilder builder = new StringBuilder();
        for (MessageComponent c : comp.iterate()) {
            if (c instanceof ProfileMessageComponent profileComp && profileComp.profile == MineTogetherChat.getOurProfile()) {
                // Mentions get red
                builder.append(ChatFormatting.RED).append(c.getMessage()).append(messageColour);
            } else {
                if (builder.isEmpty()) {
                    builder.append(messageColour);
                }
                builder.append(c.getMessage());
            }
        }
        return builder.toString();
    }

    private static ChatFormatting getMessageColour(Message message) {
        // If sender is null, it's a System/Moderator message, default to White.
        if (message.sender != null) {
            // Banned users are dark gray.
            if (message.sender.isBanned()) return ChatFormatting.DARK_GRAY;
            // Gray for our own messages.
            if (message.sender == MineTogetherChat.getOurProfile()) return ChatFormatting.GRAY;
        }
        return ChatFormatting.WHITE;
    }

    public static ChatFormatting getArrowColour(Message message) {
        if (message.sender != null) {
            // Premium users get Green.
            if (message.sender.isPremium()) return ChatFormatting.GREEN;
            // Gray for our own messages if we aren't premium.
            if (message.sender == MineTogetherChat.getOurProfile()) return ChatFormatting.GRAY;
        }
        // Otherwise White for non-premium users and System/Moderator messages.
        return ChatFormatting.WHITE;
    }

    private static ChatFormatting getUserColour(Message message) {
        // System/Moderator messages get Aqua.
        if (message.sender == null) return ChatFormatting.AQUA;

        boolean isOnSamePack = false; // TODO match these.

        // Friends get yellow, gold if they are on the same modpack.
        if (message.sender.isFriend()) return isOnSamePack ? ChatFormatting.GOLD : ChatFormatting.YELLOW;
        // Our own name is Gray.
        if (message.sender == MineTogetherChat.getOurProfile()) return ChatFormatting.GRAY;
        // People on the same pack get dark purple.
        if (isOnSamePack) return ChatFormatting.DARK_PURPLE;

        // Otherwise, White.
        return ChatFormatting.WHITE;
    }
}
