package net.creeperhost.minetogether.gui.list;

import net.creeperhost.minetogether.gui.serverlist.data.Friend;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatComponentText;

public class GuiListEntryFriend extends GuiListEntry
{

    private final Friend friend;

    public GuiListEntryFriend(GuiList list, Friend friend)
    {
        super(list);
        this.friend = friend;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
    {
        this.mc.fontRenderer.drawString(friend.getName(), x + 5, y + 5, 16777215);
        this.mc.fontRenderer.drawString(new ChatComponentText(friend.isAccepted() ? "Accepted" : "Pending").getFormattedText(), x + 5, y + 5 + 10, 16777215);
    }

    public Friend getFriend()
    {
        return friend;
    }
}
