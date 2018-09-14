package net.creeperhost.minetogether.proxy;

import net.creeperhost.minetogether.gui.chat.ingame.GuiNewChatOurs;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public interface IProxy
{
    void registerKeys();

    void openFriendsGui();

    UUID getUUID();

    void startChat();

    void disableIngameChat();

    void enableIngameChat();
}
