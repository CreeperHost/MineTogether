package net.creeperhost.minetogetherlib.chat;

public enum ChatConnectionStatus
{
    VERIFIED("Verified", "GREEN"),
    CONNECTING("Connecting", "GOLD"),
    VERIFYING("Verifying", "GOLD"),
    DISCONNECTED("Disconnected", "RED"),
    BANNED("Banned", "BLACK"),
    NICKNAME_IN_USE("Disconnected", "RED"),
    NOT_IN_CHANNEL("Not in channel", "RED");

    public final String display;
    public final String colour;

    ChatConnectionStatus(String display, String colour)
    {
        this.display = display;
        this.colour = colour;
    }
}
