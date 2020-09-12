package net.creeperhost.minetogether.gui.list;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.data.Profile;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.data.Friend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class GuiListEntryFriend extends GuiListEntry
{
    private final Friend friend;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;
    private boolean wasHovering;
    private final GuiFriendsList friendsList;
    ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(CreeperHost.MOD_ID, "textures/icon2.png");
    private Profile profile = null;

    public GuiListEntryFriend(GuiFriendsList friendsListIn, GuiList list, Friend friend)
    {
        super(list);
        friendsList = friendsListIn;
        this.friend = friend;
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRendererObj.getStringWidth(cross);
        this.profile = friend.getProfile();
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
        
        this.mc.fontRendererObj.drawString(friend.getName(), x + 5, y + 5, 16777215);
        this.mc.fontRendererObj.drawString(new TextComponentString(TextFormatting.GRAY + (friend.isAccepted() ? (profile != null && profile.isOnline() ? "Online" : "Offline") : "Pending")).getText(), x + 5, y + 5 + 10, 16777215);
        
        int transparentString = (int) (transparency * 254) << 24;
        
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        this.mc.fontRendererObj.drawStringWithShadow(cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationCreeperLogo);

        if((profile != null && profile.isOnline()))
        {
            GlStateManager.color(0, 1, 0, 1);
            Gui.drawModalRectWithCustomSizedTexture(listWidth + x - 14, y + 20, 0.0F, 0.0F, 10, 10, 10F, 10F);
            GlStateManager.resetColor();
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        
        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
            friendsList.setHoveringText("Click here to remove friend");
        }
        else if ((profile != null && profile.isOnline()) && (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 2 + x && mouseY >= y && mouseY <= y + 27)) {
            wasHovering = true;
            friendsList.setHoveringText("Click here to invite friend to private channel");
        }
        else if (wasHovering)
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
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
    {
        int listWidth = list.getListWidth();
        if (x >= listWidth - stringWidth - 4 && x <= listWidth - 5 && y >= 0 && y <= 7)
        {
            friendsList.removeFriend(friend);
            wasHovering = false;
            friendsList.setHoveringText(null);
            return false;
        }
        else if ((profile != null && profile.isOnline()) && (x >= listWidth - stringWidth - 4 && x <= listWidth - 2 && y >= 0 && y <= 27))
        {
            friendsList.inviteGroupChat(friend);
            wasHovering = false;
            friendsList.setHoveringText(null);
            return false;
        }
        return super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, x, y);
    }
}
