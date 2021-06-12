package net.creeperhost.minetogether.module.chat.screen.listentries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.MutedListScreen;
import net.creeperhost.minetogethergui.lists.ScreenList;
import net.creeperhost.minetogethergui.lists.ScreenListEntry;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ListEntryMuted extends ScreenListEntry
{
    private final Profile profile;
    private final MutedListScreen mutedListScreen;
    private boolean wasHovering;
    private final String cross = new String(Character.toChars(10006));
    private final int crossWidth = Minecraft.getInstance().font.width(cross);

    public ListEntryMuted(MutedListScreen mutedListScreen, ScreenList screenList, Profile profile)
    {
        super(screenList);
        this.mutedListScreen = mutedListScreen;
        this.profile = profile;
    }

    private float transparency = 0.5F;


    @Override
    public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
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

        this.mc.font.draw(matrixStack, profile.getUserDisplay(), x + 5, y + 5, 16777215);

        int transparentString = (int) (transparency * 254) << 24;

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        this.mc.font.drawShadow(matrixStack, cross, listWidth + x - crossWidth - 4, y, 0xFF0000 + transparentString);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();

        if (mouseX >= listWidth + x - crossWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            wasHovering = true;
            mutedListScreen.setHoveringText("Click here to unmute");
        } else if (wasHovering)
        {
            wasHovering = false;
            mutedListScreen.setHoveringText(null);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
    {
        int listWidth = ((list.getWidth() - list.getRowWidth()) / 2) + list.getRowWidth();

        int yTop = list.getRowTop(this);

        if (mouseX >= listWidth - crossWidth - 4 && mouseX <= listWidth - 5 && mouseY - yTop >= 0 && mouseY - yTop <= 7)
        {
            ChatModule.unmuteUser(profile.getLongHash());
            profile.setMuted(false);
            ChatHandler.knownUsers.update(profile);
            mutedListScreen.refreshMutedList();
            wasHovering = false;
            mutedListScreen.setHoveringText(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
    }
}
