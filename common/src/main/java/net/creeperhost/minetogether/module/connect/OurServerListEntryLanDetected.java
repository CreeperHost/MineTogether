package net.creeperhost.minetogether.module.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.resources.language.I18n;

public class OurServerListEntryLanDetected extends ServerSelectionList.NetworkServerEntry
{
    final LanServerInfoConnect serverData;

    public OurServerListEntryLanDetected(JoinMultiplayerScreen joinMultiplayerScreen, LanServerInfoConnect lanServer)
    {
        super(joinMultiplayerScreen, lanServer);
        serverData = lanServer;
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int listWidth, int l, int m, int n, int o, boolean bl, float f)
    {
        this.minecraft.font.draw(poseStack, I18n.get("minetogether.connect.friendentry.title"), x + 32 + 3, y + 1, 16777215);
        this.minecraft.font.draw(poseStack, serverData.getFriend().getChosenName(), x + 32 + 3, y + 12, 8421504);
        this.minecraft.font.draw(poseStack, ChatFormatting.DARK_GRAY + serverData.getFriend().getDisplayName(), x + listWidth - this.minecraft.font.width(serverData.getFriend().getDisplayName()) - 20, y + 12, 16777215);
    }
}
