package net.creeperhost.creeperhost.gui.hacky;

import net.minecraft.client.gui.ServerListEntryNormal;

public interface IServerListEntryWrapper
{
  void draw(ServerListEntryNormal entry, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering);
}
