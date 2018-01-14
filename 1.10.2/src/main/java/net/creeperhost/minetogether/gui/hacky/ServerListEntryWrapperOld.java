package net.creeperhost.minetogether.gui.hacky;

import net.minecraft.client.gui.ServerListEntryNormal;

public class ServerListEntryWrapperOld implements IServerListEntryWrapper
{

    @Override
    public void draw(ServerListEntryNormal entry, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        entry.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering);
    }
}
