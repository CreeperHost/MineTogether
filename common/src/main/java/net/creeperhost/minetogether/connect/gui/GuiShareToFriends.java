package net.creeperhost.minetogether.connect.gui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.netty.NettyClient;
import net.creeperhost.minetogether.gui.LoadingSpinner;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.orderform.OrderGui;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.SliderState;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;
import static net.minecraft.ChatFormatting.*;

public class GuiShareToFriends implements GuiProvider {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("MT Connect Requests Thread %d").setDaemon(true).build());
    private static final Logger LOGGER = LogManager.getLogger();
    private GameType gameMode = GameType.SURVIVAL;
    private boolean commands = false;
    private int maxPlayers = 2;
    private double extraPlayers = maxPlayers - 1;
    private final SliderState playersState = SliderState.forSlider(() -> extraPlayers / (maxPlayers - 1), pos -> extraPlayers = (pos * (maxPlayers - 1)), () -> -1D / (maxPlayers - 1));

    private CompletableFuture<?> getPlayersTask = null;
    private boolean playerCheckError = false;
    private boolean noPlayerLimit = false;

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return new GuiRectangle(gui).fill(0xC0000000);
    }

    @Override
    public void buildGui(ModularGui gui) {
        IntegratedServer integratedServer = gui.mc().getSingleplayerServer();
        gameMode = integratedServer.getDefaultGameType();
        commands = integratedServer.getWorldData().getAllowCommands();

        gui.initFullscreenGui();
        GuiElement<?> root = gui.getRoot();

        GuiRectangle bounds = new GuiRectangle(root);//.border(0xFFFF0000);
        Constraints.size(bounds, 340, 226);
        Constraints.center(bounds, root);

        GuiTexture mineTogetherLogo = new GuiTexture(root, MTTextures.get("minetogether_connect"));
        Constraints.size(mineTogetherLogo, 256, 64);
        Constraints.placeInside(mineTogetherLogo, bounds, Constraints.LayoutPos.TOP_CENTER, 0, -15);

        GuiText shareInfo = new GuiText(root, new TranslatableComponent("minetogether.connect.open.connect_intro").withStyle(BLUE))
                .constrain(WIDTH, match(bounds.get(WIDTH)))
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(shareInfo, mineTogetherLogo, Constraints.LayoutPos.BOTTOM_CENTER, 0, -8);

        GuiText connectInfo = new GuiText(root, new TranslatableComponent("minetogether.connect.open.connect_security").withStyle(GRAY))
                .constrain(WIDTH, match(bounds.get(WIDTH)))
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(connectInfo, shareInfo, Constraints.LayoutPos.BOTTOM_CENTER, 0, 5);

        GuiText settingsInfo = new GuiText(root, new TranslatableComponent("minetogether.connect.open.settings"))
                .constrain(WIDTH, match(root.get(WIDTH)))
                .constrain(HEIGHT, literal(9));
        Constraints.placeOutside(settingsInfo, connectInfo, Constraints.LayoutPos.BOTTOM_CENTER, 0, 5);

        GuiButton gameModeButton = MTStyle.Flat.button(root, TextComponent.EMPTY)
                .onPress(() -> gameMode = GameType.values()[(gameMode.ordinal() + 1) % GameType.values().length])
                .constrain(TOP, relative(settingsInfo.get(BOTTOM), 5))
                .constrain(LEFT, match(bounds.get(LEFT)))
                .constrain(RIGHT, midPoint(bounds.get(LEFT), bounds.get(RIGHT), -5))
                .constrain(HEIGHT, literal(16));
        gameModeButton.getLabel().setTextSupplier(() -> new TranslatableComponent("selectWorld.gameMode").append(": ").append(gameMode.getShortDisplayName()));

        GuiButton cheatsButton = MTStyle.Flat.button(root, TextComponent.EMPTY)
                .onPress(() -> commands = !commands)
                .constrain(TOP, relative(settingsInfo.get(BOTTOM), 5))
                .constrain(RIGHT, match(bounds.get(RIGHT)))
                .constrain(LEFT, midPoint(bounds.get(LEFT), bounds.get(RIGHT), 5))
                .constrain(HEIGHT, literal(16));
        cheatsButton.getLabel().setTextSupplier(() -> new TranslatableComponent("selectWorld.allowCommands").append(": ").append(commands ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));

        //Players
        GuiRectangle sliderBg = new GuiRectangle(root)
                .setEnabled(() -> getPlayersTask == null)
                .border(0xFF909090)
                .constrain(LEFT, match(gameModeButton.get(LEFT)))
                .constrain(RIGHT, match(cheatsButton.get(RIGHT)))
                .constrain(TOP, relative(gameModeButton.get(BOTTOM), 5))
                .constrain(HEIGHT, literal(16));

        GuiSlider slider = new GuiSlider(sliderBg, Axis.X)
                .setSliderState(playersState);
        Constraints.bind(slider, sliderBg, 1);

        sliderBg.fill(() -> slider.isMouseOver() || slider.isDragging() ? 0xFF202020 : 0xFF000000);

        GuiRectangle handle = new GuiRectangle(slider)
                .border(0xFF00FF00)
                .fill(0xFF007700)
                .constrain(WIDTH, literal(14));
        GuiRectangle handle2 = new GuiRectangle(handle)
                .border(0xFF00FF00)
                .fill(0xFF007700)
                .constrain(WIDTH, literal(6))
                .constrain(HEIGHT, match(handle.get(HEIGHT)));
        Constraints.center(handle2, handle);

        slider.installSlider(handle)
                .bindSliderWidth();

        GuiText maxPlayersText = new GuiText(sliderBg)
                .constrain(WIDTH, relative(sliderBg.get(WIDTH), -10))
                .constrain(HEIGHT, literal(16))
                .setTextSupplier(() -> new TranslatableComponent("minetogether.connect.open.max_players").append(": ").append(playersDisplay(getPlayersSetting())));
        Constraints.center(maxPlayersText, sliderBg);

        //Loading Spinner / error info
        LoadingSpinner spinner = new LoadingSpinner(root)
                .setEnabled(() -> getPlayersTask != null);
        Constraints.size(spinner, 64, 64);
        Constraints.placeOutside(spinner, settingsInfo, Constraints.LayoutPos.BOTTOM_CENTER, 0, 35);

        GuiText getPlayersError = new GuiText(root, new TranslatableComponent("minetogether.connect.open.max_players_error").withStyle(ChatFormatting.RED))
                .setEnabled(() -> playerCheckError)
                .setWrap(true)
                .constrain(WIDTH, match(bounds.get(WIDTH)))
                .autoHeight();
        Constraints.placeOutside(getPlayersError, sliderBg, Constraints.LayoutPos.BOTTOM_CENTER, 0, 5);

        //Supporter Info
        GuiText supporterInfo = new GuiText(sliderBg)
                .setEnabled(() -> !playerCheckError && !noPlayerLimit)
                .setWrap(true)
                .constrain(WIDTH, match(bounds.get(WIDTH)))
                .autoHeight();
        supporterInfo.setTextSupplier(() -> new TranslatableComponent("minetogether.connect.open.mt_supporter_info").withStyle(supporterInfo.isMouseOver() ? UNDERLINE : GRAY).withStyle(supporterInfo.isMouseOver() ? BLUE : GRAY));
        Constraints.placeOutside(supporterInfo, sliderBg, Constraints.LayoutPos.BOTTOM_CENTER, 0, 5);

        GuiButton supporterLink = new GuiButton(sliderBg)
                .setEnabled(supporterInfo::isEnabled)
                .onClick(() -> openLink(gui, "https://minetogether.io/profile/subscriptions"));
        Constraints.bind(supporterLink, supporterInfo);

        //Order page link
        GuiText orderInfo = new GuiText(sliderBg)
                .setWrap(true)
                .constrain(WIDTH, match(bounds.get(WIDTH)))
                .autoHeight();
        orderInfo.setTextSupplier(() -> new TranslatableComponent("minetogether.connect.open.order_info").withStyle(orderInfo.isMouseOver() ? UNDERLINE : GRAY).withStyle(orderInfo.isMouseOver() ? BLUE : GRAY));
        Constraints.placeOutside(orderInfo, supporterInfo, Constraints.LayoutPos.BOTTOM_CENTER, 0, 5);

        GuiButton orderLink = new GuiButton(sliderBg)
                .setEnabled(orderInfo::isEnabled)
                .onClick(() -> gui.mc().setScreen(new OrderGui.Screen(gui.getScreen(), true)));
        Constraints.bind(orderLink, orderInfo);

        //Go / Cancel
        GuiButton openButton = MTStyle.Flat.buttonPrimary(root, new TranslatableComponent("minetogether.connect.open.start"))
                .onPress(() -> openWorld(gui))
                .constrain(BOTTOM, match(bounds.get(BOTTOM)))
                .constrain(LEFT, match(bounds.get(LEFT)))
                .constrain(RIGHT, midPoint(bounds.get(LEFT), bounds.get(RIGHT), -5))
                .constrain(HEIGHT, literal(16));

        GuiButton cancelButton = MTStyle.Flat.button(root, new TranslatableComponent("gui.cancel"))
                .onPress(() -> gui.getScreen().onClose())
                .constrain(BOTTOM, match(bounds.get(BOTTOM)))
                .constrain(RIGHT, match(bounds.get(RIGHT)))
                .constrain(LEFT, midPoint(bounds.get(LEFT), bounds.get(RIGHT), 5))
                .constrain(HEIGHT, literal(16));

        startPlayersCheck();
        gui.onTick(() -> {
            if (getPlayersTask != null && getPlayersTask.isDone()) {
                getPlayersTask = null;
            }
        });
    }

    private int getPlayersSetting() {
        int value = 1 + Math.max(0, Math.min((int) Math.round(extraPlayers), maxPlayers));
        return value >= 100 ? Integer.MAX_VALUE : value;
    }

    private Component playersDisplay(int players) {
        return new TextComponent(players == Integer.MAX_VALUE ? "\u221E" : String.valueOf(players));
    }

    private void openWorld(ModularGui gui) {
        gui.mc().setScreen(null);
        gui.mc().gui.getChat().addMessage(new TranslatableComponent("minetogether.connect.open.attempting"));
        ConnectHandler.publishToFriends(gameMode, commands, getPlayersSetting());
    }

    private void startPlayersCheck() {
        getPlayersTask = CompletableFuture.runAsync(() -> {
            try {
                JWebToken token = MineTogetherSession.getDefault().getTokenAsync().get();
                maxPlayers = NettyClient.getMaxPlayers(ConnectHandler.getEndpoint(), token);
                if (maxPlayers == -1) {
                    noPlayerLimit = true;
                    maxPlayers = 100;
                }
                extraPlayers = maxPlayers - 1;
                playerCheckError = false;
            } catch (Throwable e) {
                playerCheckError = true;
                maxPlayers = 2;
                extraPlayers = 1;
                LOGGER.error("An error occurred while attempting to check max players", e);
            }
        }, EXECUTOR);
    }

    private void openLink(ModularGui gui, String url) {
        gui.mc().setScreen(new ConfirmLinkScreen((bl) -> {
            if (bl) {
                Util.getPlatform().openUri(url);
            }

            gui.mc().setScreen(gui.getScreen());
        }, url, true));
    }

    public static class Screen extends ModularGuiScreen {
        public Screen(net.minecraft.client.gui.screens.Screen parentScreen) {
            super(new GuiShareToFriends(), parentScreen);
            modularGui.setPauseScreen(true);
        }
    }
}
