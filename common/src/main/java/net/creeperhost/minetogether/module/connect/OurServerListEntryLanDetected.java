package net.creeperhost.minetogether.module.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.mixin.MixinServerSelectionList;
import net.creeperhost.minetogether.mixin.MixinServerSelectionListAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;

import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OurServerListEntryLanDetected extends ServerSelectionList.NetworkServerEntry
{
    private final JoinMultiplayerScreen joinMultiplayerScreen;
    final LanServerInfoConnect serverData;
    final ServerData fullServerData;
    private final ServerSelectionList mixinServerSelectionList;
    private final static Pattern pattern = Pattern.compile("^(\\d+)/(\\d+)$");

    public OurServerListEntryLanDetected(JoinMultiplayerScreen joinMultiplayerScreen, LanServerInfoConnect lanServer, ServerSelectionList mixinServerSelectionList)
    {
        super(joinMultiplayerScreen, lanServer);
        this.joinMultiplayerScreen = joinMultiplayerScreen;
        serverData = lanServer;
        fullServerData = new ServerData("", lanServer.getAddress(), false);
        this.mixinServerSelectionList = mixinServerSelectionList;
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int listWidth, int l, int m, int n, int o, boolean bl, float f)
    {
        boolean full = false;
        if (!this.fullServerData.pinged) {
            this.fullServerData.pinged = true;
            this.fullServerData.ping = -2L;
            this.fullServerData.motd = TextComponent.EMPTY;
            this.fullServerData.status = TextComponent.EMPTY;
            MixinServerSelectionListAccessor.getPingThreadPool().submit(() -> {
                try {
                    this.joinMultiplayerScreen.getPinger().pingServer(this.fullServerData, () -> {
                    });
                } catch (UnknownHostException var2) {
                    this.fullServerData.ping = -1L;
                } catch (Exception var3) {
                    this.fullServerData.ping = -1L;
                }

            });
        }

        Matcher matcher = pattern.matcher(fullServerData.status.getString());
        if(matcher.find()) {
            String curStr = matcher.group(1);
            String maxStr = matcher.group(2);

            int max = 999;
            int cur = 0;

            try {
                max = Integer.parseInt(maxStr);
                cur = Integer.parseInt(curStr);
            } catch (Exception ignored) {
            }

            if (cur >= max - 1) {
                full = true;
            }
        }

        String fullString = "FULL";

        this.minecraft.font.draw(poseStack, I18n.get("minetogether.connect.friendentry.title"), listWidth + l - this.minecraft.font.width(fullString), y + 1, 16777215);
        this.minecraft.font.draw(poseStack, I18n.get("minetogether.connect.friendentry.title"), listWidth + 32 + 3, y + 1, 16777215);
        this.minecraft.font.draw(poseStack, serverData.getFriend().getChosenName(), listWidth + 32 + 3, y + 12, 8421504);
        this.minecraft.font.draw(poseStack, ChatFormatting.DARK_GRAY + serverData.getFriend().getDisplayName(), listWidth + l - this.minecraft.font.width(serverData.getFriend().getDisplayName()) - 20, y + 12, 16777215);
    }
}
