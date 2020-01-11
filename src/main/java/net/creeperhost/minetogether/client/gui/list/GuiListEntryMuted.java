package net.creeperhost.minetogether.client.gui.list;

import com.mojang.blaze3d.platform.GlStateManager;
import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiFriendsList;

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
        stringWidth = this.mc.fontRenderer.getStringWidth(cross);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void render(int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
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
        
        this.mc.fontRenderer.drawString(muted, x + 5, y + 5, 16777215);
        
        int transparentString = (int) (transparency * 254) << 24;
        
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        this.mc.fontRenderer.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
        GlStateManager.disableAlphaTest();
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
    
    public String getMuted()
    {
        return muted;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
    {
        int listWidth = list.getWidth();
        if (mouseX >= listWidth - stringWidth - 4 && mouseX <= listWidth - 5 && mouseY >= 0 && mouseY <= 7)
        {
            System.out.println("fnmposnfop");
            friendsListgui.unmutePlayer(muted);
            wasHovering = false;
            friendsListgui.setHoveringText(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
    }
}
