package net.creeperhost.minetogether.client.gui.hacky;

import net.minecraft.client.gui.screen.ServerSelectionList;

public class ServerListEntryWrapperNew implements IServerListEntryWrapper
{
    @Override
    public void draw(ServerSelectionList.Entry entry, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering)
    {
        try
        {
            if (entry != null)
            {
                entry.render(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering, 0);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
