package net.creeperhost.minetogether.connectv2.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.connectv2.ConnectHandlerV2;
import net.creeperhost.minetogether.connectv2.RemoteServer;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.Component;

/**
 * Created by brandon3055 on 21/04/2023
 */
public class FriendServerEntry extends ServerSelectionList.NetworkServerEntry {

    public final RemoteServer remoteServer;
    public final Profile friendProfile;

    protected FriendServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, RemoteServer remoteServer) {
        super(joinMultiplayerScreen, new LanServer("Dummy Server", "0.0.0.0"));
        this.remoteServer = remoteServer;
        this.friendProfile = ConnectHandlerV2.getServerProfile(remoteServer);
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int listWidth, int l, int m, int n, int o, boolean bl, float f) {
        //TODO
//        if (!this.fullServerData.pinged) {
//            this.fullServerData.pinged = true;
//            this.fullServerData.ping = -2L;
//            this.fullServerData.motd = Component.empty();
//            this.fullServerData.status = Component.empty();
//            ServerSelectionListAccessor.getPingThreadPool().submit(() -> {
//                try {
//                    this.joinMultiplayerScreen.getPinger().pingServer(this.fullServerData, () -> {
//                    });
//                } catch (UnknownHostException var2) {
//                    this.fullServerData.ping = -1L;
//                } catch (Exception var3) {
//                    this.fullServerData.ping = -1L;
//                }
//
//            });
//        }
//
//        full = false;
//
//        Matcher matcher = pattern.matcher(fullServerData.status.getString());
//        if (matcher.find()) {
//            String curStr = matcher.group(1);
//            String maxStr = matcher.group(2);
//
//            int max = 999;
//            int cur = 0;
//
//            try {
//                max = Integer.parseInt(maxStr);
//                cur = Integer.parseInt(curStr);
//            } catch (Exception ignored) {
//            }
//
//            if (cur >= max) {
//                full = true;
//            }
//        }
//
//        if (full) {
//            //if (bl) joinMultiplayerScreen.setToolTip(FULL_TOOLTOP_LIST);
//            this.minecraft.font.draw(poseStack, FULL_COMPONENT, listWidth + l - fullComponentWidth - 20, y + 1, 16777215);
//        }
//
        minecraft.font.draw(poseStack, Component.translatable("minetogether.connect.friendentry.title"), listWidth + 32 + 3, y + 1, 16777215);
        minecraft.font.draw(poseStack, getFriendName(), listWidth + 32 + 3, y + 12, 8421504);
        minecraft.font.draw(poseStack, ChatFormatting.DARK_GRAY + getDisplayName(), listWidth + l - this.minecraft.font.width(getDisplayName()) - 20, y + 12, 16777215);
    }

    public String getFriendName() {
        return friendProfile.hasFriendName() ? friendProfile.getFriendName() : "[Name Unavailable]";
    }

    public String getDisplayName() {
        return friendProfile.isFriend() && friendProfile.hasFriendName() ? friendProfile.getFriendName() : friendProfile.getDisplayName();
    }
}
