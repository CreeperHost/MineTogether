package net.creeperhost.minetogether.module.chat.screen.listentries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.minetogethergui.lists.ScreenList;
import net.creeperhost.minetogethergui.lists.ScreenListEntry;
import net.creeperhost.minetogetherlib.chat.data.Friend;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ListEntryFriend extends ScreenListEntry
{
    private final Friend friend;
    private final FriendsListScreen friendsListScreen;
    private final Profile profile;
    private boolean wasHovering;
    private final String cross = new String(Character.toChars(10006));
    private final int crossWidth = Minecraft.getInstance().font.width(cross);
    private final ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Constants.MOD_ID, "textures/icon2.png");

    public ListEntryFriend(FriendsListScreen friendsListScreen, ScreenList screenList, Profile profile)
    {
        super(screenList);
        this.friendsListScreen = friendsListScreen;
        friend = new Friend(profile.friendName, profile.friendCode, true);
        this.profile = profile;
    }

    private float transparency = 0.5F;


    @Override
    public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        if (isSelected)
        {
            if (transparency <= 1.0F) transparency += 0.04;
        } else
        {
            if (transparency >= 0.5F) transparency -= 0.04;
        }

        this.mc.font.draw(matrixStack, friend.getName(), x + 5, y + 5, 16777215);
        if(profile != null) {
            this.mc.font.draw(matrixStack, ChatFormatting.DARK_GRAY + profile.getUserDisplay(), x + listWidth - this.mc.font.width(profile.getUserDisplay()) - 20, y + 7, 16777215);
            this.mc.font.draw(matrixStack, new TranslatableComponent(ChatFormatting.GRAY + (friend.isAccepted() ? (profile != null && profile.isOnline() ? ChatFormatting.DARK_GREEN + "Online" : "Offline") : "Pending")).getString(), x + 5, y + 17, 16777215);
        }
        int transparentString = (int) (transparency * 254) << 24;

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        this.mc.font.drawShadow(matrixStack, cross, listWidth + x - crossWidth - 4, y, 0xFF0000 + transparentString);

        Minecraft.getInstance().getTextureManager().bind(resourceLocationCreeperLogo);

        if((profile != null && profile.isOnline()))
        {
            RenderSystem.color4f(0, 1, 0, 1);
            Screen.blit(matrixStack, listWidth + x - 14, y + 20, 0.0F, 0.0F, 10, 10, 10, 10);
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();

        if (mouseX >= listWidth + x - crossWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
            friendsListScreen.setHoveringText("Click here to remove friend");
        } else if ((profile != null && profile.isOnline()) && mouseX >= listWidth + x - crossWidth - 4 && mouseX <= listWidth - 2 + x && mouseY >= y && mouseY <= y + 27)
        {
            wasHovering = true;
            friendsListScreen.setHoveringText("Click here to invite friend to private channel");
        } else if (wasHovering)
        {
            wasHovering = false;
            friendsListScreen.setHoveringText(null);
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

        if (mouseX >= listWidth - crossWidth - 4 && mouseX <= listWidth - 5 && mouseY - yTop >= 0 && mouseY - yTop <= 7)
        {
            friendsListScreen.removeFriend(friend);
            wasHovering = false;
            friendsListScreen.setHoveringText(null);
            return true;
        } else if ((profile != null && profile.isOnline()) && mouseX >= listWidth - crossWidth - 4 && mouseX <= listWidth - 2 && mouseY - yTop >= 0 && mouseY - yTop <= 27)
        {
            friendsListScreen.inviteGroupChat(friend);
            wasHovering = false;
            friendsListScreen.setHoveringText(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
    }
}
