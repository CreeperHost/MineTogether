package net.creeperhost.minetogether.gui.list;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.serverlist.data.Friend;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentString;

public class GuiListEntryMuted extends GuiListEntry
{
    private final String muted;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;
    private boolean wasHovering;
    private final GuiFriendsList friendsListgui;
    
    public GuiListEntryMuted(GuiFriendsList friendsListIngui, GuiList list, String muted)
    {
        super(list);
        friendsListgui = friendsListIngui;
        this.muted = muted;
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRendererObj.getStringWidth(cross);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        if (isSelected)
        {
            if (transparency <= 1.0F)
                transparency += 0.04;
        } else
        {
            if (transparency >= 0.5F)
                transparency -= 0.04;
        }
        
        this.mc.fontRendererObj.drawString(muted, x + 5, y + 5, 16777215);
        
        int transparentString = (int) (transparency * 254) << 24;
        
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        this.mc.fontRendererObj.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        
        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
            friendsListgui.setHoveringText("Click here to unmute");
        } else if (wasHovering)
        {
            wasHovering = false;
            friendsListgui.setHoveringText(null);
        }
    }
    
    public String getFriend()
    {
        return muted;
    }
    
    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
    {
        int listWidth = list.getListWidth();
        if (x >= listWidth - stringWidth - 4 && x <= listWidth - 5 && y >= 0 && y <= 7)
        {
            CreeperHost.mutedUsers.remove(muted);
            wasHovering = false;
            friendsListgui.setHoveringText(null);
            return false;
        }
        return super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, x, y);
    }
}
