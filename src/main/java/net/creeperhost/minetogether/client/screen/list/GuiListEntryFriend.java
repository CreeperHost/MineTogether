package net.creeperhost.minetogether.client.screen.list;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.client.screen.serverlist.gui.FriendsListScreen;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.lib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class GuiListEntryFriend extends GuiListEntry
{
    private final Friend friend;
    private final String cross;
    private final int stringWidth;
    private float transparency = 0.5F;
    private boolean wasHovering;
    private final FriendsListScreen friendsList;
    ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Constants.MOD_ID, "textures/icon2.png");
    private Profile profile = null;
    
    public GuiListEntryFriend(FriendsListScreen friendsListIn, GuiList list, Friend friend)
    {
        super(list);
        friendsList = friendsListIn;
        this.friend = friend;
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRenderer.getStringWidth(cross);
        this.profile = friend.getProfile();
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
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
        
        this.mc.fontRenderer.drawString(matrixStack, friend.getName(), x + 5, y + 5, 16777215);
        if(profile != null) {
            this.mc.fontRenderer.drawString(matrixStack, TextFormatting.DARK_GRAY + profile.getUserDisplay(), x + listWidth - this.mc.fontRenderer.getStringWidth(profile.getUserDisplay()) - 20, y + 7, 16777215);
            this.mc.fontRenderer.drawString(matrixStack, new StringTextComponent(TextFormatting.GRAY + (friend.isAccepted() ? (profile != null && profile.isOnline() ? TextFormatting.DARK_GREEN + "Online" : "Offline") : "Pending")).getText(), x + 5, y + 17, 16777215);
        }
        int transparentString = (int) (transparency * 254) << 24;
        
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        this.mc.fontRenderer.drawStringWithShadow(matrixStack, cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
        
        Minecraft.getInstance().getTextureManager().bindTexture(resourceLocationCreeperLogo);

        if((profile != null && profile.isOnline()))
        {
            RenderSystem.color4f(0, 1, 0, 1);
            Screen.blit(matrixStack, listWidth + x - 14, y + 20, 0.0F, 0.0F, 10, 10, 10, 10);
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        
        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
            friendsList.setHoveringText("Click here to remove friend");
        } else if ((profile != null && profile.isOnline()) && mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 2 + x && mouseY >= y && mouseY <= y + 27)
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
    public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
    {
        int listWidth = ((list.getWidth() - list.getRowWidth()) / 2) + list.getRowWidth();
        
        int yTop = list.getRowTop(this);
        
        if (mouseX >= listWidth - stringWidth - 4 && mouseX <= listWidth - 5 && mouseY - yTop >= 0 && mouseY - yTop <= 7)
        {
            friendsList.removeFriend(friend);
            wasHovering = false;
            friendsList.setHoveringText(null);
            return false;
        } else if ((profile != null && profile.isOnline()) && mouseX >= listWidth - stringWidth - 4 && mouseX <= listWidth - 2 && mouseY - yTop >= 0 && mouseY - yTop <= 27)
        {
            friendsList.inviteGroupChat(friend);
            wasHovering = false;
            friendsList.setHoveringText(null);
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
    }
}
