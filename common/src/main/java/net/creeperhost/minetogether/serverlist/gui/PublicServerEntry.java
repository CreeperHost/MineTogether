package net.creeperhost.minetogether.serverlist.gui;

import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.minetogether.util.EnumFlag;
import net.creeperhost.polylib.client.screen.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

// Due to Java limitations, we cannot extend ServerSelectionList.OnlineServerEntry without
// an enclosing class of ServerSelectionList or something whiche extends it.
// TODO We may be able to do this instead of copying OnlineServerEntry
public class PublicServerEntry extends ServerSelectionList.Entry {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ServerData serverData;
    private ServerSelectionList serverSelectionList;
    private JoinMultiplayerScreenPublic joinMultiplayerScreen;
    @Nullable
    private byte[] lastIconBytes;
    private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LogUtils.getLogger())).build());
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
    private final Component CANT_RESOLVE_TEXT = (Component.translatable("multiplayer.status.cannot_resolve")).withStyle(ChatFormatting.DARK_RED);
    private final Component NO_CONNECTION_TOOLTIP = Component.translatable("multiplayer.status.no_connection");
    private final Component CANT_CONNECT_TEXT = (Component.translatable("multiplayer.status.cannot_connect")).withStyle(ChatFormatting.DARK_RED);
    private final Component PINGING_TOOLTIP = Component.translatable("multiplayer.status.pinging");
    private final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private final FaviconTexture icon;
    private long lastClickTime;

    private ResourceLocation flags = new ResourceLocation(MineTogether.MOD_ID, "textures/flags/flags.png");

    public PublicServerEntry(JoinMultiplayerScreenPublic joinMultiplayerScreen, ServerSelectionList serverSelectionList, ServerData serverData) {
        this.serverData = serverData;
        this.joinMultiplayerScreen = joinMultiplayerScreen;
        this.serverSelectionList = serverSelectionList;
        this.icon = FaviconTexture.forServer(this.minecraft.getTextureManager(), serverData.ip);
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
        if (!this.serverData.pinged) {
            this.serverData.pinged = true;
            this.serverData.ping = -2L;
            this.serverData.motd = Component.empty();
            this.serverData.status = Component.empty();
            THREAD_POOL.submit(() ->
            {
                try {
                    this.joinMultiplayerScreen.getPinger().pingServer(this.serverData, () ->
                    {
                        this.minecraft.execute(this::updateServerList);
                    });
                } catch (UnknownHostException var2) {
                    this.serverData.ping = -1L;
                    this.serverData.motd = CANT_RESOLVE_TEXT;
                } catch (Exception var3) {
                    this.serverData.ping = -1L;
                    this.serverData.motd = CANT_CONNECT_TEXT;
                }

            });
        }
        //Kill off not compatable version check
        boolean bl2 = false;//this.serverData.protocol != SharedConstants.getCurrentVersion().getProtocolVersion();
        graphics.drawString(this.minecraft.font, this.serverData.name, (k + 32 + 3), (j + 1), 16777215);
        List<FormattedCharSequence> list = this.minecraft.font.split(this.serverData.motd, l - 32 - 2);

        for (int p = 0; p < Math.min(list.size(), 2); ++p) {
            Font var10000 = this.minecraft.font;
            FormattedCharSequence var10002 = list.get(p);
            int var10003 = (k + 32 + 3);
            int var10004 = j + 12;
            this.minecraft.font.getClass();
            graphics.drawString(var10000, var10002, var10003, (var10004 + 9 * p), 8421504);
        }

        Component component = bl2 ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status;
        int q = this.minecraft.font.width(component);
        graphics.drawString(this.minecraft.font, component, (k + l - q - 15 - 2), (j + 1), 8421504);
        int r = 0;
        int z;
        List list5;
        Component component5;
        if (bl2) {
            z = 5;
            component5 = Component.empty();
            list5 = this.serverData.playerList;
        } else if (this.serverData.pinged && this.serverData.ping != -2L) {
            if (this.serverData.ping < 0L) {
                z = 5;
            } else if (this.serverData.ping < 150L) {
                z = 0;
            } else if (this.serverData.ping < 300L) {
                z = 1;
            } else if (this.serverData.ping < 600L) {
                z = 2;
            } else if (this.serverData.ping < 1000L) {
                z = 3;
            } else {
                z = 4;
            }

            if (this.serverData.ping < 0L) {
                component5 = NO_CONNECTION_TOOLTIP;
                list5 = Collections.emptyList();
            } else {
                component5 = Component.translatable("multiplayer.status.ping", new Object[] { this.serverData.ping });
                list5 = this.serverData.playerList;
            }
        } else {
            r = 1;
            z = (int) (Util.getMillis() / 100L + (long) (i * 2) & 7L);
            if (z > 4) {
                z = 8 - z;
            }

            component5 = PINGING_TOOLTIP;
            list5 = Collections.emptyList();
        }

        graphics.blit(GUI_ICONS_LOCATION, k + l - 15, j, (float)(r * 10), (float)(176 + z * 8), 10, 8, 256, 256);
        byte[] bs = this.serverData.getIconBytes();
        if (!Arrays.equals(bs, this.lastIconBytes)) {
            if (this.uploadServerIcon(bs)) {
                this.lastIconBytes = bs;
            } else {
                this.serverData.setIconBytes(null);
                this.updateServerList();
            }
        }

        this.drawIcon(graphics, k, j, this.icon.textureLocation());

        int aa = n - k;
        int ab = o - j;
        if (aa >= l - 15 && aa <= l - 5 && ab >= 0 && ab <= 8) {
            this.joinMultiplayerScreen.setToolTip(Collections.singletonList(component5));
        } else if (aa >= l - q - 15 - 2 && aa <= l - 15 - 2 && ab >= 0 && ab <= 8) {
            this.joinMultiplayerScreen.setToolTip(list5);
        }

        if (this.minecraft.options.touchscreen().get() || bl) {
            graphics.fill(k, j, k + 32, j + 32, -1601138544);
            int ac = n - k;
            int ad = o - j;
            if (this.canJoin()) {
                if (ac < 32 && ac > 16) {
                    graphics.blit(ICON_OVERLAY_LOCATION, k, j, 0.0F, 32.0F, 32, 32, 256, 256);
                } else {
                    graphics.blit(ICON_OVERLAY_LOCATION, k, j, 0.0F, 0.0F, 32, 32, 256, 256);
                }
            }

            if (i < this.joinMultiplayerScreen.getServers().size() - 1) {
                if (ac < 16 && ad > 16) {
                    graphics.blit(ICON_OVERLAY_LOCATION, k, j, 64.0F, 32.0F, 32, 32, 256, 256);
                } else {
                    graphics.blit(ICON_OVERLAY_LOCATION, k, j, 64.0F, 0.0F, 32, 32, 256, 256);
                }
            }
        }
        //Our server data
        if (getServerData() != null) {
            Server server = getServerData().server;
            EnumFlag flag = server.getFlag();
            String applicationURL = server.applicationUrl;
            if (flag != null) {
                RenderSystem.setShaderTexture(0, flags);
                int flagWidth = 16;
                int flagHeight = flag.height / (flag.width / flagWidth);
                ScreenHelper.drawScaledCustomSizeModalRect(k + l - 5 - flagWidth, j + 30 - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);

                if (n >= k + l - 5 - flagWidth && n <= k + l - 5 && o >= j - 10 - flagHeight && o <= j - flagHeight + flagHeight) {
                    List<Component> tooltipList = new ArrayList<>();

                    String countryName = Countries.COUNTRIES.getOrDefault(flag.name(), flag.name());
                    tooltipList.add(Component.translatable(countryName));
                    joinMultiplayerScreen.setToolTip(tooltipList);
                }
            }
        }
    }

    private boolean uploadServerIcon(@Nullable byte[] bs) {
        if (bs == null) {
            this.icon.clear();
        } else {
            try {
                this.icon.upload(NativeImage.read(bs));
            } catch (Throwable var3) {
                LOGGER.error("Invalid icon for server {} ({})", new Object[]{this.serverData.name, this.serverData.ip, var3});
                return false;
            }
        }

        return true;
    }

    private boolean canJoin() {
        return true;
    }

    public void updateServerList() {
        this.joinMultiplayerScreen.getServers().save();
    }

    protected void drawIcon(GuiGraphics graphics, int i, int j, ResourceLocation resourceLocation) {
        RenderSystem.enableBlend();
        graphics.blit(resourceLocation, i, j, 0.0F, 0.0F, 32, 32, 32, 32);
        RenderSystem.disableBlend();
    }

    public ServerDataPublic getServerData() {
        if (serverData instanceof ServerDataPublic) return (ServerDataPublic) serverData;
        return null;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        double f = d - (double) serverSelectionList.getRowLeft();
        if (f <= 32.0D) {
            if (f < 32.0D && f > 16.0D && this.canJoin()) {
                this.joinMultiplayerScreen.setSelected(this);
                this.joinMultiplayerScreen.joinSelectedServer();
                return true;
            }
        }

        this.joinMultiplayerScreen.setSelected(this);
        if (Util.getMillis() - this.lastClickTime < 250L) {
            this.joinMultiplayerScreen.joinSelectedServer();
        }

        this.lastClickTime = Util.getMillis();
        return false;
    }

    @Override
    public Component getNarration() {
        return Component.translatable("narrator.select", new Object[] { this.serverData.name });
    }
}
