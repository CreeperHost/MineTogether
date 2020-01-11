package net.creeperhost.minetogether.client.gui.list;

import com.mojang.blaze3d.platform.GlStateManager;
import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.data.Friend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class GuiListEntryFriend extends GuiListEntry
{
    private final Friend friend;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;
    private boolean wasHovering;
    private final GuiFriendsList friendsList;
    ResourceLocation resourceLocationCreeperLogo = new ResourceLocation("creeperhost", "textures/icon2.png");
    
    public GuiListEntryFriend(GuiFriendsList friendsListIn, GuiList list, Friend friend)
    {
        super(list);
        friendsList = friendsListIn;
        this.friend = friend;
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
        
        this.mc.fontRenderer.drawString(friend.getName(), x + 5, y + 5, 16777215);
        this.mc.fontRenderer.drawString(new StringTextComponent(friend.isAccepted() ? "Accepted" : "Pending").getText(), x + 5, y + 5 + 10, 16777215);
        
        int transparentString = (int) (transparency * 254) << 24;
        
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        this.mc.fontRenderer.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
        
        Minecraft.getInstance().getTextureManager().bindTexture(resourceLocationCreeperLogo);
        
        GlStateManager.color4f(0, 1, 0, 1);
        Screen.blit(listWidth + x - 14,  y + 20, 0.0F, 0.0F, 10, 10, 10, 10);
        
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
        
        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
            friendsList.setHoveringText("Click here to remove friend");
        } else if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 2 + x && mouseY >= y && mouseY <= y + 27)
        {
            wasHovering = true;
            friendsList.setHoveringText("Click here to invite friend to private channel");
        } else if (wasHovering)
        {
            wasHovering = false;
            friendsList.setHoveringText(null);
        }
    }
    
    public Friend getFriend()
    {
        return friend;
    }
    
    @Override
    public boolean mouseClicked(double x, double y, int p_mouseClicked_5_)
    {
        int listWidth = list.getWidth();
        if (x >= listWidth - stringWidth - 4 && x <= listWidth - 5 && y >= 0 && y <= 7)
        {
            friendsList.removeFriend(friend);
            wasHovering = false;
            friendsList.setHoveringText(null);
            return false;
        }
        else if (x >= listWidth - stringWidth - 4 && x <= listWidth - 2 && y >= 0 && y <= 27)
        {
            friendsList.inviteGroupChat(friend);
            wasHovering = false;
            friendsList.setHoveringText(null);
            return false;
        }
        return false;
    }
}
