package net.creeperhost.minetogether.connect.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.RemoteServer;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.mixin.connect.ServerSelectionListAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by brandon3055 on 21/04/2023
 */
public class FriendServerEntry extends ServerSelectionList.NetworkServerEntry {
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
    private static final Component INCOMPATIBLE_TOOLTIP = new TranslatableComponent("multiplayer.status.incompatible");
    private static final Component NO_CONNECTION_TOOLTIP = new TranslatableComponent("multiplayer.status.no_connection");
    private static final Component PINGING_TOOLTIP = new TranslatableComponent("multiplayer.status.pinging");

    private final JoinMultiplayerScreen screen;
    public final RemoteServer remoteServer;
    public final Profile friendProfile;
    private final ServerListAppender listAppender;

    private final ResourceLocation iconLocation;
    @Nullable
    private String lastIconB64;
    @Nullable
    private DynamicTexture icon;

    protected FriendServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, RemoteServer remoteServer, Profile friendProfile, ServerListAppender listAppender) {
        super(joinMultiplayerScreen, new LanServer("Dummy Server", "0.0.0.0"));
        this.screen = joinMultiplayerScreen;
        this.remoteServer = remoteServer;
        this.friendProfile = friendProfile;
        this.listAppender = listAppender;
        iconLocation = new ResourceLocation("servers/" + friendProfile.getFullHash().toLowerCase(Locale.ROOT) + "/icon");
    }

    @Override                                                 //Yes. y, then x. This is correct. wtf...
    public void render(PoseStack poseStack, int entryIndex, int y, int x, int entryWidth, int m, int mouseX, int mouseY, boolean selected, float f) {
        //Do Ping
        if (!remoteServer.pinged) {
            remoteServer.pinged = true;
            remoteServer.ping = -2L;
            remoteServer.motd = TextComponent.EMPTY;
            remoteServer.status = TextComponent.EMPTY;
            ServerSelectionListAccessor.getPingThreadPool().submit(() -> {
                try {
                    listAppender.pingServer(remoteServer, friendProfile);
                } catch (Exception var2) {
                    remoteServer.ping = -1L;
                }
            });
        }

        //Draw Server Title
        this.minecraft.font.draw(poseStack, new TranslatableComponent("minetogether.connect.friend.server.title", getDisplayName()), (float) (x + 32 + 3), (float) (y + 1), 16777215);

        //Draw MOTD
        List<FormattedCharSequence> list = this.minecraft.font.split(this.remoteServer.motd, entryWidth - 32 - 2);
        for (int line = 0; line < Math.min(list.size(), 2); ++line) {
            Font var10000 = this.minecraft.font;
            FormattedCharSequence var10002 = list.get(line);
            float var10003 = (float) (x + 32 + 3);
            int var10004 = y + 12;
            Objects.requireNonNull(this.minecraft.font);
            var10000.draw(poseStack, var10002, var10003, (float) (var10004 + 9 * line), 8421504);
        }

        boolean versionMismatch = this.remoteServer.protocol != SharedConstants.getCurrentVersion().getProtocolVersion();
        //Num Players or Version Mismatch text
        Component statusText = versionMismatch ? this.remoteServer.version.copy().withStyle(ChatFormatting.RED) : this.remoteServer.status;
        //Draw Status
        int statusWidth = this.minecraft.font.width(statusText);
        this.minecraft.font.draw(poseStack, statusText, (float) (x + entryWidth - statusWidth - 15 - 2), (float) (y + 1), 8421504);

        int signalTexSelect = 0; //0 = Signal Bars, 1 = "Scanning" bars
        int singnalBarsInvrse; //Higher 0 = full bars, 5 = no bars
        List<Component> playersToolTip;
        Component statusToolTip;
        if (versionMismatch) {
            singnalBarsInvrse = 5;
            statusToolTip = INCOMPATIBLE_TOOLTIP;
            playersToolTip = this.remoteServer.playerList;
        } else if (this.remoteServer.pinged && this.remoteServer.ping != -2L) {
            if (this.remoteServer.ping < 0L) {
                singnalBarsInvrse = 5;
            } else if (this.remoteServer.ping < 150L) {
                singnalBarsInvrse = 0;
            } else if (this.remoteServer.ping < 300L) {
                singnalBarsInvrse = 1;
            } else if (this.remoteServer.ping < 600L) {
                singnalBarsInvrse = 2;
            } else if (this.remoteServer.ping < 1000L) {
                singnalBarsInvrse = 3;
            } else {
                singnalBarsInvrse = 4;
            }

            if (this.remoteServer.ping < 0L) {
                statusToolTip = NO_CONNECTION_TOOLTIP;
                playersToolTip = Collections.emptyList();
            } else {
                statusToolTip = new TranslatableComponent("multiplayer.status.ping", this.remoteServer.ping);
                playersToolTip = this.remoteServer.playerList;
            }
        } else {
            signalTexSelect = 1;
            singnalBarsInvrse = (int) (Util.getMillis() / 100L + (entryIndex * 2L) & 7L);
            if (singnalBarsInvrse > 4) {
                singnalBarsInvrse = 8 - singnalBarsInvrse;
            }

            statusToolTip = PINGING_TOOLTIP;
            playersToolTip = Collections.emptyList();
        }

        //Draw Signal / Scanning Bars.
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
        GuiComponent.blit(poseStack, x + entryWidth - 15, y, (float) (signalTexSelect * 10), (float) (176 + singnalBarsInvrse * 8), 10, 8, 256, 256);

        //Update server icon.
        String string = this.remoteServer.getIconB64();
        if (!Objects.equals(string, this.lastIconB64)) {
            if (this.uploadServerIcon(string)) {
                this.lastIconB64 = string;
            } else {
                this.remoteServer.setIconB64(null);
            }
        }

        //Draw server icon
        if (this.icon == null) {
            this.drawIcon(poseStack, x, y, ICON_MISSING);
        } else {
            this.drawIcon(poseStack, x, y, this.iconLocation);
        }

        int t = mouseX - x;
        int u = mouseY - y;
        if (t >= entryWidth - 15 && t <= entryWidth - 5 && u >= 0 && u <= 8) {
            //Draw Status Tool Tip
            this.screen.setToolTip(Collections.singletonList(statusToolTip));
        } else if (t >= entryWidth - statusWidth - 15 - 2 && t <= entryWidth - 15 - 2 && u >= 0 && u <= 8) {
            //Draw Players Tool Tip
            this.screen.setToolTip(playersToolTip);
        }

        if (this.minecraft.options.touchscreen || selected) {
            minecraft.getTextureManager().bind(ICON_OVERLAY_LOCATION);
            GuiComponent.fill(poseStack, x, y, x + 32, y + 32, 0xa0909090);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int v = mouseX - x;
            //Draw "Join Arrow"
            if (v < 32 && v > 16) {
                GuiComponent.blit(poseStack, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
            } else {
                GuiComponent.blit(poseStack, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
            }
        }
    }

    public String getDisplayName() {
        return friendProfile.isFriend() ? friendProfile.getFriendName() : friendProfile.getDisplayName();
    }

    protected void drawIcon(PoseStack poseStack, int i, int j, ResourceLocation resourceLocation) {
        minecraft.getTextureManager().bind(resourceLocation);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 32, 32, 32, 32);
        RenderSystem.disableBlend();
    }

    private boolean uploadServerIcon(@Nullable String string) {
        if (string == null) {
            this.minecraft.getTextureManager().release(this.iconLocation);
            if (this.icon != null && this.icon.getPixels() != null) {
                this.icon.getPixels().close();
            }

            this.icon = null;
        } else {
            try {
                NativeImage nativeImage = NativeImage.fromBase64(string);
                Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");
                if (this.icon == null) {
                    this.icon = new DynamicTexture(nativeImage);
                } else {
                    this.icon.setPixels(nativeImage);
                    this.icon.upload();
                }

                this.minecraft.getTextureManager().register((ResourceLocation) this.iconLocation, (AbstractTexture) this.icon);
            } catch (Throwable var3) {
//                LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, var3);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listAppender.getServerList() != null) {
            double f = mouseX - listAppender.getServerList().getRowLeft();
            if (f < 32.0 && f > 16.0) {
                this.screen.setSelected(this);
                this.screen.joinSelectedServer();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
