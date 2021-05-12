package net.creeperhost.minetogetherlib.chat;

public enum ChatConnectionStatus
{
    CONNECTED("Connected", "GREEN"),
    CONNECTING("Connecting", "GOLD"),
    DISCONNECTED("Disconnected", "RED"),
    NOT_IN_CHANNEL("Not in channel", "RED"),
    BANNED("Banned", "BLACK");

    public final String display;
    public final String colour;

    ChatConnectionStatus(String display, String colour)
    {
        this.display = display;
        this.colour = colour;
    }
}
