package net.creeperhost.minetogether.connect;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.mixin.connect.MixinServerSelectionListAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OurServerListEntryLanDetected extends ServerSelectionList.NetworkServerEntry {

    private final JoinMultiplayerScreen joinMultiplayerScreen;
    final LanServerInfoConnect serverData;
    final ServerData fullServerData;
    private final ServerSelectionList mixinServerSelectionList;
    private final static Pattern pattern = Pattern.compile("^(\\d+)/(\\d+)$");
    private final static Component FULL_COMPONENT = new TranslatableComponent("minetogether.connect.join.full.descript").withStyle(ChatFormatting.RED);
    private final static int fullComponentWidth = Minecraft.getInstance().font.width(FULL_COMPONENT);
    public final static Component FULL_MESSAGE_COMPONENT = new TranslatableComponent("minetogether.connect.join.full");
    private final static List<Component> FULL_MESSAGE_COMPONENT_LIST = ImmutableList.of(FULL_MESSAGE_COMPONENT);
    private boolean full = false;

    public OurServerListEntryLanDetected(JoinMultiplayerScreen joinMultiplayerScreen, LanServerInfoConnect lanServer, ServerSelectionList mixinServerSelectionList) {
        super(joinMultiplayerScreen, lanServer);
        this.joinMultiplayerScreen = joinMultiplayerScreen;
        serverData = lanServer;
        fullServerData = new ServerData("", lanServer.getAddress(), false);
        this.mixinServerSelectionList = mixinServerSelectionList;
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int listWidth, int l, int m, int n, int o, boolean bl, float f) {
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

        full = false;

        Matcher matcher = pattern.matcher(fullServerData.status.getString());
        if (matcher.find()) {
            String curStr = matcher.group(1);
            String maxStr = matcher.group(2);

            int max = 999;
            int cur = 0;

            try {
                max = Integer.parseInt(maxStr);
                cur = Integer.parseInt(curStr);
            } catch (Exception ignored) {
            }

            if (cur >= max) {
                full = true;
            }
        }

        if (full) {
            //if (bl) joinMultiplayerScreen.setToolTip(FULL_TOOLTOP_LIST);
            this.minecraft.font.draw(poseStack, FULL_COMPONENT, listWidth + l - fullComponentWidth - 20, y + 1, 16777215);
        }
        this.minecraft.font.draw(poseStack, I18n.get("minetogether.connect.friendentry.title"), listWidth + 32 + 3, y + 1, 16777215);
        this.minecraft.font.draw(poseStack, serverData.getFriend().getChosenName(), listWidth + 32 + 3, y + 12, 8421504);
        this.minecraft.font.draw(poseStack, ChatFormatting.DARK_GRAY + serverData.getFriend().getDisplayName(), listWidth + l - this.minecraft.font.width(serverData.getFriend().getDisplayName()) - 20, y + 12, 16777215);
    }

    public boolean canBeJoined() {
        return !full;
    }
}
