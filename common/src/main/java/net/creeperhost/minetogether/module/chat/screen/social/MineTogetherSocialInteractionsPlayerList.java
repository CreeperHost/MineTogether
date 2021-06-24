package net.creeperhost.minetogether.module.chat.screen.social;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogetherlib.chat.KnownUsers;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.ArrayList;
import java.util.List;

public class MineTogetherSocialInteractionsPlayerList extends ContainerObjectSelectionList<ProfileEntry>
{
    public MineTogetherSocialinteractionsScreen socialInteractionsScreen;
    public Minecraft minecraft;
    public List<ProfileEntry> list = Lists.newArrayList();

    public MineTogetherSocialInteractionsPlayerList(MineTogetherSocialinteractionsScreen socialInteractionsScreen, Minecraft minecraft, int i, int j, int k, int l, int m)
    {
        super(minecraft, i, j, k, l, m);
        this.socialInteractionsScreen = socialInteractionsScreen;
        this.minecraft = minecraft;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        double d = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int)((double)this.getRowLeft() * d), (int)((double)(this.height - this.y1) * d), (int)((double)(this.getScrollbarPosition() + 6) * d), (int)((double)(this.height - (this.height - this.y1) - this.y0 - 4) * d));
        super.render(poseStack, i, j, f);
        RenderSystem.disableScissor();
        if(list != null)
        {
            list.forEach(profileEntry ->
            {
                if (profileEntry != null) profileEntry.renderTooltips(poseStack, i, j);
            });
        }
    }

    public void updateList(String filter, MineTogetherSocialinteractionsScreen.Page page)
    {
        list.clear();
        clearEntries();
        List<Profile> copy = new ArrayList<>();
        switch (page)
        {
            case ALL:
                copy = KnownUsers.getProfiles().get();
                break;
            case FRIENDS:
                copy = KnownUsers.getFriends();
                break;
            case BLOCKED:
                copy = KnownUsers.getMuted();
                break;
            case PARTY:
                copy = KnownUsers.getPartyMembers();
                break;
        }

        if(!copy.isEmpty()) {
            for (Profile profile : copy) {
                ProfileEntry profileEntry = new ProfileEntry(profile, socialInteractionsScreen);
                if (filter != null && !filter.isEmpty()) {
                    if (profile.getUserDisplay().toLowerCase().contains(filter) || profile.getUserDisplay().contains(filter)) {
                        addEntry(profileEntry);
                        list.add(profileEntry);
                    }
                } else {
                    addEntry(profileEntry);
                    list.add(profileEntry);
                }
            }
        }
    }
}
