package net.creeperhost.minetogether.chat;

import com.google.common.collect.ImmutableMap;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.minecraft.ChatFormatting;

import java.util.Map;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ChatConstants {

    public static final Map<IrcState, ChatFormatting> STATE_FORMAT_LOOKUP = ImmutableMap.of(
            IrcState.DISCONNECTED, ChatFormatting.RED,
            IrcState.CONNECTING, ChatFormatting.GOLD,
            IrcState.RECONNECTING, ChatFormatting.GOLD,
            IrcState.CONNECTED, ChatFormatting.GREEN,
            IrcState.CRASHED, ChatFormatting.RED,
            IrcState.BANNED, ChatFormatting.BLACK,
            IrcState.VERIFYING, ChatFormatting.GOLD
    );
    public static final Map<IrcState, String> STATE_DESC_LOOKUP = ImmutableMap.of(
            IrcState.DISCONNECTED, "Disconnected",
            IrcState.CONNECTING, "Connecting",
            IrcState.RECONNECTING, "Reconnecting",
            IrcState.CONNECTED, "Connected",
            IrcState.CRASHED, "Engine crashed",
            IrcState.BANNED, "Banned",
            IrcState.VERIFYING, "Verifying"
    );
    public static final Map<IrcState, String> STATE_SUGGESTION_LOOKUP = ImmutableMap.of(
            IrcState.DISCONNECTED, "minetogether:screen.chat.suggestion.disconnected",
            IrcState.CONNECTING, "minetogether:screen.chat.suggestion.connecting",
            IrcState.RECONNECTING, "minetogether:screen.chat.suggestion.reconnecting",
            IrcState.CRASHED, "minetogether:screen.chat.suggestion.crashed",
            IrcState.BANNED, "minetogether:screen.chat.suggestion.banned",
            IrcState.VERIFYING, "minetogether:screen.chat.suggestion.verifying"
    );
}
