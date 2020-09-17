package net.creeperhost.minetogether.client.screen.hacky;

import com.mojang.blaze3d.matrix.MatrixStack;
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
                MatrixStack matrixStack = new MatrixStack();
                entry.render(matrixStack, slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isHovering, 0);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
