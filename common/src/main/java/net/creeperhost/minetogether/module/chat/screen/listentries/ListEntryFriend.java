package net.creeperhost.minetogether.module.chat.screen.listentries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.polylib.client.screen.widget.ScreenList;
import net.creeperhost.polylib.client.screen.widget.ScreenListEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ListEntryFriend extends ScreenListEntry
{
    private final FriendsListScreen friendsListScreen;
    private final Profile profile;
    private final ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Constants.MOD_ID, "textures/icon2.png");

    public ListEntryFriend(FriendsListScreen friendsListScreen, ScreenList screenList, Profile profile)
    {
        super(screenList);
        this.friendsListScreen = friendsListScreen;
        this.profile = profile;
    }

    private float transparency = 0.5F;

    @Override
    public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        if (isSelected)
        {
            if (transparency <= 1.0F) transparency += 0.04;
        }
        else
        {
            if (transparency >= 0.5F) transparency -= 0.04;
        }

        String friendName = profile.getFriendName();
        boolean ellipsis = false;
        while (mc.font.width(friendName) >= listWidth - 15)
        {
            friendName = friendName.substring(0, friendName.length() - 1);
            ellipsis = true;
        }
        if (ellipsis) friendName += "...";

        this.mc.font.draw(matrixStack, friendName, x + 5, y + 4, 16777215);
        if (profile != null)
            this.mc.font.draw(matrixStack, new TranslatableComponent(ChatFormatting.GRAY + (profile.isFriend() ? (profile != null && profile.isOnline() ? ChatFormatting.DARK_GREEN + "Online" : "Offline") : "Pending")).getString(), x + 5, y + 15, 16777215);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resourceLocationCreeperLogo);

        RenderSystem.disableBlend();
    }

    public Profile getProfile()
    {
        return profile;
    }
}
