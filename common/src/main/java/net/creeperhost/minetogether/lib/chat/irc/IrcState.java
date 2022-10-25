package net.creeperhost.minetogether.lib.chat.irc;

/**
 * Created by covers1624 on 13/8/20.
 */
public enum IrcState {
    DISCONNECTED,
    CONNECTING,
    RECONNECTING,
    CONNECTED,
    CRASHED,
    BANNED,
    VERIFYING;

    public boolean isCrashed() {
        return this == CRASHED;
    }

    public boolean canReconnect() {
        return !isCrashed();
    }

    public boolean isConnected() {
        return this == CONNECTED;
    }
}
