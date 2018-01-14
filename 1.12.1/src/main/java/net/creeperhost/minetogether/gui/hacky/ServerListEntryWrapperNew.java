package net.creeperhost.minetogether.gui.hacky;

import net.minecraft.client.gui.ServerListEntryNormal;

public class ServerListEntryWrapperNew implements IServerListEntryWrapper
{

    @Override
    public void draw(ServerListEntryNormal entry, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        entry.func_192634_a(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering, 0);
    }
}
