package net.creeperhost.minetogether.client.gui.hacky;

import net.minecraft.client.gui.screen.ServerSelectionList;

public interface IServerListEntryWrapper
{
    void draw(ServerSelectionList.Entry entry, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering);
}
