package net.creeperhost.minetogether.serverlist.gui;

import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.creeperhost.polylib.client.modulargui.sprite.Material;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 22/10/2023
 */
public class ServerEntry extends GuiElement<ServerEntry> implements BackgroundRender {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LogUtils.getLogger())).build());

    private final ResourceLocation iconLocation;
    private final ServerDataPublic serverData;
    private final ServerListGui gui;
    private final int index;
    @Nullable
    private String lastIconB64;
    @Nullable
    private DynamicTexture icon;
    private long lastClicked = 0;
    private int tick = 0;

    public ServerEntry(@NotNull GuiParent<?> parent, ServerDataPublic data, ServerListGui gui, int index) {
        super(parent);
        this.serverData = data;

        this.iconLocation = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(serverData.ip) + "/icon");
        AbstractTexture abstractTexture = mc().getTextureManager().getTexture(this.iconLocation, MissingTextureAtlasSprite.getTexture());
        if (abstractTexture != MissingTextureAtlasSprite.getTexture() && abstractTexture instanceof DynamicTexture) {
            this.icon = (DynamicTexture)abstractTexture;
        }

        this.gui = gui;
        this.index = index;
        this.constrain(HEIGHT, literal(24));

        GuiRectangle background = new GuiRectangle(this);
        background.fill(() -> MTStyle.Flat.listEntryBackground(background.isMouseOver() || gui.selected == data));
        Constraints.bind(background, this);

        GuiTexture iconTexture = new GuiTexture(background, () -> Material.fromRawTexture(iconLocation))
                .constrain(TOP, relative(background.get(TOP), 1))
                .constrain(BOTTOM, relative(background.get(BOTTOM), -1))
                .constrain(LEFT, relative(background.get(LEFT), 1))
                .constrain(WIDTH, literal(background.ySize() - 2));

        GuiTexture signal = new GuiTexture(background, MTTextures.getter(this::getSignalIcon))
                .constrain(TOP, relative(background.get(TOP), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(WIDTH, literal(12))
                .constrain(HEIGHT, literal(12));

        GuiTexture flag = new GuiTexture(background, this::getFlag)
                .setTooltipSingle(() -> new TextComponent(Countries.COUNTRIES.getOrDefault(serverData.server.location.countryCode, serverData.server.location.countryCode)))
                .setTooltipDelay(0)
                .constrain(BOTTOM, relative(background.get(BOTTOM), -2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(HEIGHT, literal(9))
                .constrain(WIDTH, dynamic(() -> flagWidth(9)));

        GuiText signalInfo = new GuiText(background, this::signalInfo)
                .setWrap(false)
                .constrain(TOP, relative(background.get(TOP), 3))
                .constrain(RIGHT, relative(signal.get(LEFT), -2))
//                .constrain(RIGHT, relative(flag.get(LEFT), -2))
                .constrain(WIDTH, dynamic(() -> (double) font().width(signalInfo())))
                .constrain(HEIGHT, literal(8));

        GuiText status = new GuiText(background, () -> data.status)
                .setWrap(false)
                .setTooltip(this::playersToolTip)
                .setTooltipDelay(0)
                .constrain(BOTTOM, relative(background.get(BOTTOM), -2))
                .constrain(RIGHT, relative(flag.get(LEFT), -2))
                .constrain(WIDTH, dynamic(() -> (double) Math.max(font().width(data.status), 16)))
                .constrain(HEIGHT, literal(8));

        GuiText name = new GuiText(background, new TextComponent(data.name))
                .setAlignment(Align.MIN)
                .constrain(TOP, relative(background.get(TOP), 2))
                .constrain(LEFT, relative(iconTexture.get(RIGHT), 2))
                .constrain(RIGHT, relative(signalInfo.get(LEFT), -4))
                .constrain(HEIGHT, literal(8));

        GuiText motd = new GuiText(background, () -> data.motd)
                .setAlignment(Align.MIN)
                .constrain(TOP, relative(name.get(BOTTOM), 3))
                .constrain(LEFT, relative(iconTexture.get(RIGHT), 2))
                .constrain(RIGHT, relative(status.get(LEFT), -4))
                .constrain(HEIGHT, literal(8));

        GuiButton connectBtn = new GuiButton(background)
                .setEnabled(background::isMouseOver)
                .onPress(() -> gui.join(serverData));
        Constraints.bind(connectBtn, iconTexture);
        GuiTexture connectBtnTex = new GuiTexture(connectBtn, () -> MTTextures.get(connectBtn.isMouseOver() ? "buttons/join_highlight" : "buttons/join"));
        Constraints.bind(connectBtnTex, connectBtn);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver()) {
            if (gui.selected != serverData) {
                gui.selected = serverData;
                mc().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            } else if (System.currentTimeMillis() - lastClicked < 250L) {
                gui.join(serverData);
                mc().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            }
            lastClicked = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Override
    public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        render.rect(xMin(), yMax(), xSize(), 1, MTStyle.Flat.listEntryBackground(true));
        render.rect(xMin(), yMax(), xSize(), 1, MTStyle.Flat.listEntryBackground(true));

        if (!this.serverData.pinged){
            doPing();
        }

        String string = this.serverData.getIconB64();
        if (!Objects.equals(string, this.lastIconB64)) {
            if (this.uploadServerIcon(string)) {
                this.lastIconB64 = string;
            } else {
                this.serverData.setIconB64((String)null);
//                this.updateServerList();
            }
        }
    }

    public void update() {
        tick+=4;
        if (!this.serverData.pinged && tick > index) {
            doPing();
        }
    }

    private void doPing() {
        if (!this.serverData.pinged) {
            this.serverData.pinged = true;
            this.serverData.ping = -2L;
            this.serverData.motd = TextComponent.EMPTY;
            this.serverData.status = TextComponent.EMPTY;
            THREAD_POOL.submit(() ->
            {
                try {
                    gui.getPinger().pingServer(this.serverData, () -> {});
                    gui.sortDirty = true;
                } catch (UnknownHostException var2) {
                    this.serverData.ping = -1L;
                    this.serverData.motd = new TranslatableComponent("multiplayer.status.cannot_resolve").withStyle(ChatFormatting.DARK_RED);
                } catch (Exception var3) {
                    this.serverData.ping = -1L;
                    this.serverData.motd = new TranslatableComponent("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);
                }
            });
        }
    }

    private boolean uploadServerIcon(@Nullable String string) {
        if (string == null) {
            mc().getTextureManager().release(this.iconLocation);
            if (this.icon != null && this.icon.getPixels() != null) {
                this.icon.getPixels().close();
            }

            this.icon = null;
        } else {
            try {
                NativeImage nativeImage = NativeImage.fromBase64(string);
                Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                if (this.icon == null) {
                    this.icon = new DynamicTexture(nativeImage);
                } else {
                    this.icon.setPixels(nativeImage);
                    this.icon.upload();
                }

                mc().getTextureManager().register(this.iconLocation, this.icon);
            } catch (Throwable var3) {
                LOGGER.error("Invalid icon for server {} ({})", new Object[]{this.serverData.name, this.serverData.ip, var3});
                return false;
            }
        }

        return true;
    }

    private String getSignalIcon() {
        if (this.serverData.pinged && this.serverData.ping != -2L) {
            if (this.serverData.ping < 0L) {
                return "signal/signal_0";
            } else if (this.serverData.ping < 150L) {
                return "signal/signal_5";
            } else if (this.serverData.ping < 300L) {
                return "signal/signal_4";
            } else if (this.serverData.ping < 600L) {
                return "signal/signal_3";
            } else if (this.serverData.ping < 1000L) {
                return "signal/signal_2";
            } else {
                return "signal/signal_1";
            }
        } else {
            int l = (int) (Util.getMillis() / 100L & 7L);
            if (l > 4) {
                l = 8 - l;
            }
            return "signal/scan_" + l;
        }
    }

    private Component signalInfo() {
        if (this.serverData.pinged && this.serverData.ping != -2L) {
            return this.serverData.ping < 0L ? new TranslatableComponent("multiplayer.status.no_connection").withStyle(ChatFormatting.DARK_RED) : new TranslatableComponent("multiplayer.status.ping", new Object[]{this.serverData.ping});
        } else {
            return new TranslatableComponent("multiplayer.status.pinging");
        }
    }

    private List<Component> playersToolTip() {
        return this.serverData.ping < 0L ? Collections.emptyList() : this.serverData.playerList;
    }

    private Material getFlag() {
        Server.Location location = serverData.server.location;
        String code = location.countryCode.toUpperCase(Locale.ROOT);
        if (Countries.COUNTRIES.containsKey(code)) {
            code = code.toLowerCase(Locale.ROOT);
        } else {
            code = "unknown";
        }
        return MTTextures.get("flags/" + code);
    }

    private double flagWidth(double height) {
        TextureAtlasSprite sprite = getFlag().sprite();
        return (sprite.getWidth() / (double)sprite.getHeight()) * height;
    }
}
