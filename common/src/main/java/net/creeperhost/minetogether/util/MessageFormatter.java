package net.creeperhost.minetogether.util;

import com.mojang.serialization.DataResult;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.message.MessageComponent;
import net.creeperhost.minetogether.lib.chat.message.ProfileMessageComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.ClickEvent.Action;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.ChatFormatting.RED;
import static net.minecraft.ChatFormatting.RESET;

/**
 * Created by covers1624 on 11/8/22.
 */
public class MessageFormatter {

    public static final HoverEvent.Action<Component> SHOW_URL_PREVIEW = new HoverEvent.Action<>("show_url_preview", true, ComponentSerialization.CODEC, DataResult::success);
    public static final String CLICK_NAME = "CE:CLICK_NAME";

    private static final Pattern URL_PATTERN = Pattern.compile(
            //         schema                          ipv4            OR        namespace                 port     path         ends
            //   |-----------------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
            "((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))", Pattern.CASE_INSENSITIVE);

    public static Component formatMessage(Message message) {
        ChatFormatting mc = getMessageColour(message);
        ChatFormatting ac = getArrowColour(message);
        ChatFormatting uc = getUserColour(message);

        // TODO obfuscate banned user messages.
        String sender = ac + "<" + uc + message.senderName + ac + ">" + RESET;

        return Component.literal(sender).withStyle(e -> e.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, CLICK_NAME)))
                .append(" ")
                .append(formatMessage(message.getMessage(), mc));
    }

    private static Component formatMessage(MessageComponent comp, ChatFormatting messageColour) {
        MutableComponent component = Component.literal("").withStyle(messageColour);
        for (MessageComponent c : comp.iterate()) {
            if (c instanceof ProfileMessageComponent profileComp && profileComp.profile == MineTogetherChat.getOurProfile()) {
                // Mentions get red
                component.append(Component.literal(c.getMessage()).withStyle(RED));
            } else {
                component.append(sugarLinkWithPreview(c.getMessage(), true, messageColour));
            }
        }
        return component;
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

        boolean isOnSamePack = MineTogetherChat.getOurProfile().isOnSamePack(message.sender);

        // Friends get yellow, gold if they are on the same modpack.
        if (message.sender.isFriend()) return isOnSamePack ? ChatFormatting.GOLD : ChatFormatting.YELLOW;
        // Our own name is Gray.
        if (message.sender == MineTogetherChat.getOurProfile()) return ChatFormatting.GRAY;
        // People on the same pack get dark purple.
        if (isOnSamePack) return ChatFormatting.DARK_PURPLE;

        // Otherwise, White.
        return ChatFormatting.WHITE;
    }

    //Copied from forge
    private static Component sugarLinkWithPreview(String string, boolean allowMissingHeader, ChatFormatting messageColour) {
        // Includes ipv4 and domain pattern
        // Matches an ip (xx.xxx.xx.xxx) or a domain (something.com) with or
        // without a protocol or path.
        MutableComponent ichat = null;
        Matcher matcher = URL_PATTERN.matcher(string);
        int lastEnd = 0;

        // Find all urls
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Append the previous left overs.
            String part = string.substring(lastEnd, start);
            if (part.length() > 0) {
                if (ichat == null) {
                    ichat = Component.literal(part).withStyle(messageColour);
                } else {
                    ichat.append(part);
                }
            }
            lastEnd = end;
            String url = string.substring(start, end);
            MutableComponent link = Component.literal(url);

            try {
                // Add schema so client doesn't crash.
                if ((new URI(url)).getScheme() == null) {
                    if (!allowMissingHeader) {
                        if (ichat == null) {
                            ichat = Component.literal(url);
                        } else {
                            ichat.append(url);
                        }
                        continue;
                    }
                    url = "http://" + url;
                }
            } catch (URISyntaxException e) {
                // Bad syntax bail out!
                if (ichat == null) {
                    ichat = Component.literal(url);
                } else {
                    ichat.append(url);
                }
                continue;
            }

            // Set the click event and append the link.
            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            HoverEvent hoverEvent = new HoverEvent(SHOW_URL_PREVIEW, Component.literal(url));
            link.setStyle(link.getStyle().withClickEvent(click).withHoverEvent(hoverEvent).withUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
            if (ichat == null) {
                ichat = Component.literal("");
            }
            ichat.append(link);
        }

        // Append the rest of the message.
        String end = string.substring(lastEnd);
        if (ichat == null) {
            ichat = Component.literal(end).withStyle(messageColour);
        } else if (end.length() > 0) {
            ichat.append(Component.literal(string.substring(lastEnd)).withStyle(messageColour));
        }
        return ichat;
    }
}
