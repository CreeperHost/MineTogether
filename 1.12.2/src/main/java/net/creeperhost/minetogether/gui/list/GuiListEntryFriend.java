package net.creeperhost.minetogether.gui.list;

import net.creeperhost.minetogether.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.serverlist.data.Friend;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.ForgeHooksClient;

public class GuiListEntryFriend extends GuiListEntry
{

    private final Friend friend;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;
    private boolean wasHovering;
    private final GuiFriendsList friendsList;

    public GuiListEntryFriend(GuiFriendsList friendsListIn, GuiList list, Friend friend)
    {
        super(list);
        friendsList = friendsListIn;
        this.friend = friend;
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRendererObj.getStringWidth(cross);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        if (isSelected)
        {
            if (transparency <= 1.0F)
                transparency += 0.04;
        }
        else
        {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }


        this.mc.fontRendererObj.drawString(friend.getName(), x + 5, y + 5, 16777215);
        this.mc.fontRendererObj.drawString(new TextComponentString(friend.isAccepted() ? "Accepted" : "Pending").getText(), x + 5, y + 5 + 10, 16777215);

        int transparentString = (int) (transparency * 254) << 24;

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        this.mc.fontRendererObj.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
            friendsList.setHoveringText("Click here to remove friend");
        } else if (wasHovering) {
            wasHovering = false;
            friendsList.setHoveringText(null);
        }
    }

    public Friend getFriend()
    {
        return friend;
    }
}
