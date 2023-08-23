package net.creeperhost.minetogether.chat;

import com.google.common.collect.ImmutableMap;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.minecraft.ChatFormatting;

import java.util.Map;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ChatConstants {

    public static final Map<IrcState, ChatFormatting> STATE_FORMAT_LOOKUP = ImmutableMap.<IrcState, ChatFormatting>builder()
            .put(IrcState.DISCONNECTED, ChatFormatting.RED)
            .put(IrcState.CONNECTING, ChatFormatting.GOLD)
            .put(IrcState.RECONNECTING, ChatFormatting.GOLD)
            .put(IrcState.CONNECTED, ChatFormatting.GREEN)
            .put(IrcState.CRASHED, ChatFormatting.RED)
            .put(IrcState.BANNED, ChatFormatting.BLACK)
            .put(IrcState.VERIFYING, ChatFormatting.GOLD)
            .build();
    public static final Map<IrcState, String> STATE_DESC_LOOKUP = ImmutableMap.<IrcState, String>builder()
            .put(IrcState.DISCONNECTED, "Disconnected")
            .put(IrcState.CONNECTING, "Connecting")
            .put(IrcState.RECONNECTING, "Reconnecting")
            .put(IrcState.CONNECTED, "Connected")
            .put(IrcState.CRASHED, "Engine crashed")
            .put(IrcState.BANNED, "Banned")
            .put(IrcState.VERIFYING, "Verifying")
            .build();
    public static final Map<IrcState, String> STATE_SUGGESTION_LOOKUP = ImmutableMap.<IrcState, String>builder()
            .put(IrcState.DISCONNECTED, "minetogether:screen.chat.suggestion.disconnected")
            .put(IrcState.CONNECTING, "minetogether:screen.chat.suggestion.connecting")
            .put(IrcState.RECONNECTING, "minetogether:screen.chat.suggestion.reconnecting")
            .put(IrcState.CRASHED, "minetogether:screen.chat.suggestion.crashed")
            .put(IrcState.BANNED, "minetogether:screen.chat.suggestion.banned")
            .put(IrcState.VERIFYING, "minetogether:screen.chat.suggestion.verifying")
            .build();
}
