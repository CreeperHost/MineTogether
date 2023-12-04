package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.chat.gui.FriendChatGui;
import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.oauth.KeycloakOAuth;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.*;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.creeperhost.polylib.helpers.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.literal;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.RIGHT;

/**
 * Created by brandon3055 on 02/10/2023
 */
public class SettingGui implements GuiProvider {

    private boolean showBlocked = false;
    private double blockedAnim;
    private GuiList<Profile> blockedList;

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();
        gui.setGuiTitle(new TranslatableComponent("minetogether:gui.settings.title"));

        GuiElement<?> root = gui.getRoot();
        int panelWidth = 150;

        GuiText title = new GuiText(root, gui.getGuiTitle())
                .constrain(TOP, relative(root.get(TOP), 10))
                .constrain(HEIGHT, literal(8))
                .constrain(LEFT, relative(root.get(LEFT), 10))
                .constrain(RIGHT, relative(root.get(RIGHT), -10));

        GuiElement<?> settings = new GuiElement<>(root)
                .constrain(TOP, midPoint(root.get(TOP), root.get(BOTTOM), (7*20) / -2D))
                .constrain(LEFT, dynamic(() -> buttonPanelPos(root)))
                .constrain(WIDTH, literal(panelWidth))
                .constrain(HEIGHT, literal(0));

        GuiButton enabled = MTStyle.Flat.button(settings, TextComponent.EMPTY)
                .onPress(this::toggleEnabled)
                .constrain(TOP, match(settings.get(TOP)))
                .constrain(LEFT, match(settings.get(LEFT)))
                .constrain(RIGHT, match(settings.get(RIGHT)))
                .constrain(HEIGHT, literal(16));
        enabled.getLabel().setTextSupplier(() -> new TranslatableComponent("minetogether:gui.settings.button.chat").append(state(Config.instance().chatEnabled)));

        GuiButton menuButtons = MTStyle.Flat.button(settings, TextComponent.EMPTY)
                .onPress(() -> setConfig(() -> Config.instance().mainMenuButtons ^= true))
                .constrain(TOP, relative(enabled.get(BOTTOM), 4))
                .constrain(LEFT, match(settings.get(LEFT)))
                .constrain(RIGHT, match(settings.get(RIGHT)))
                .constrain(HEIGHT, literal(16));
        menuButtons.getLabel().setTextSupplier(() -> new TranslatableComponent("minetogether:gui.settings.button.menu_buttons").append(state(Config.instance().mainMenuButtons)));

        GuiButton toasts = MTStyle.Flat.button(settings, TextComponent.EMPTY)
                .onPress(() -> setConfig(() -> Config.instance().friendNotifications ^= true))
                .constrain(TOP, relative(menuButtons.get(BOTTOM), 4))
                .constrain(LEFT, match(settings.get(LEFT)))
                .constrain(RIGHT, match(settings.get(RIGHT)))
                .constrain(HEIGHT, literal(16));
        toasts.getLabel().setTextSupplier(() -> new TranslatableComponent("minetogether:gui.settings.button.friend_toasts").append(state(Config.instance().friendNotifications)));

        GuiButton chatSliders = MTStyle.Flat.button(toasts, TextComponent.EMPTY)
                .onPress(() -> setConfig(() -> Config.instance().chatSettingsSliders ^= true))
                .constrain(TOP, relative(menuButtons.get(BOTTOM), 4))
                .constrain(LEFT, match(settings.get(LEFT)))
                .constrain(RIGHT, match(settings.get(RIGHT)))
                .constrain(HEIGHT, literal(16));
        chatSliders.getLabel().setTextSupplier(() -> new TranslatableComponent("minetogether:gui.settings.button.chat_sliders").append(state(Config.instance().chatSettingsSliders)));

        GuiButton blocked = MTStyle.Flat.button(settings, new TranslatableComponent("minetogether:gui.settings.button.blocked"))
                .onPress(() -> setConfig(() -> showBlocked ^= true))
                .constrain(TOP, relative(toasts.get(BOTTOM), 4))
                .constrain(LEFT, match(settings.get(LEFT)))
                .constrain(RIGHT, match(settings.get(RIGHT)))
                .constrain(HEIGHT, literal(16));

        GuiButton link = MTStyle.Flat.button(settings, new TranslatableComponent("minetogether:gui.settings.button.link"))
                .onPress(() -> {
                    gui.mc().setScreen(new ConfirmScreen(b -> {
                        if (b) {
                            KeycloakOAuth.main(new String[0]);
                        }
                        gui.mc().setScreen(gui.getScreen());
                    }, new TranslatableComponent("minetogether:linkaccount1"), new TranslatableComponent("minetogether:linkaccount2")));
                })
                .setDisabled(() -> MineTogetherChat.getOurProfile().hasAccount())
                .constrain(TOP, relative(blocked.get(BOTTOM), 4))
                .constrain(LEFT, match(settings.get(LEFT)))
                .constrain(RIGHT, match(settings.get(RIGHT)))
                .constrain(HEIGHT, literal(16));

        GuiButton back = MTStyle.Flat.button(settings, new TranslatableComponent("minetogether:gui.button.back"))
                .onPress(() -> gui.mc().setScreen(gui.getParentScreen()))
                .constrain(TOP, relative(link.get(BOTTOM), 24))
                .constrain(LEFT, match(settings.get(LEFT)))
                .constrain(RIGHT, match(settings.get(RIGHT)))
                .constrain(HEIGHT, literal(16));


        //Blocked Users
        GuiElement<?> blockedBg = MTStyle.Flat.contentArea(root)
                .setEnabled(() -> blockedAnim == 1)
                .constrain(LEFT, midPoint(root.get(LEFT), root.get(RIGHT), 5))
                .constrain(WIDTH, literal(panelWidth))
                .constrain(TOP, midPoint(root.get(TOP), root.get(BOTTOM), -90))
                .constrain(BOTTOM, midPoint(root.get(TOP), root.get(BOTTOM), 100));

        GuiText blockedTitle = new GuiText(blockedBg, new TranslatableComponent("minetogether:gui.settings.button.blocked").withStyle(ChatFormatting.UNDERLINE))
                .constrain(BOTTOM, relative(blockedBg.get(TOP), -3))
                .constrain(HEIGHT, literal(8))
                .constrain(LEFT, match(blockedBg.get(LEFT)))
                .constrain(RIGHT, match(blockedBg.get(RIGHT)));

        blockedList = new GuiList<Profile>(blockedBg)
                .setDisplayBuilder(BlockedEntry::new)
                .setItemSpacing(2);
        Constraints.bind(blockedList, blockedBg, 5);

        var scrollBar = MTStyle.Flat.scrollBar(blockedBg, Axis.Y);
        scrollBar.container
                .setEnabled(() -> blockedList.hiddenSize() > 0)
                .constrain(TOP, match(blockedList.get(TOP)))
                .constrain(BOTTOM, match(blockedList.get(BOTTOM)))
                .constrain(RIGHT, match(blockedBg.get(RIGHT)))
                .constrain(WIDTH, literal(4));
        scrollBar.primary
                .setScrollableElement(blockedList)
                .setSliderState(blockedList.scrollState());

        updateBlockedList();
        gui.onTick(this::tick);
        gui.onResize(this::updateBlockedList);
    }

    private void updateBlockedList() {
        blockedList.getList().clear();
        blockedList.markDirty();
        String search = "";
        for (Profile mutedProfile : MineTogetherChat.CHAT_STATE.profileManager.getMutedProfiles()) {
            if (StringUtils.isEmpty(search) || StringUtils.containsAnyIgnoreCase(mutedProfile.getDisplayName(), search)) {
                blockedList.add(mutedProfile);
            }
        }
    }

    private void tick() {
        if (showBlocked && blockedAnim < 1) {
            blockedAnim = Math.min(1, blockedAnim + 0.2);
        } else if (!showBlocked && blockedAnim > 0) {
            blockedAnim = Math.max(0, blockedAnim - 0.2);
        }
    }

    private double buttonPanelPos(GuiElement<?> root) {
        double partial = (showBlocked ? 0.2 : -0.2) * Minecraft.getInstance().getFrameTime();
        double anim = MathUtil.clamp(blockedAnim + partial, 0, 1);
        return (root.xCenter() - 75D - (80D * anim));
    }

    private static Component state(boolean state) {
        if (state) {
            return new TranslatableComponent("minetogether:gui.settings.button.enabled").withStyle(ChatFormatting.GREEN);
        }
        return new TranslatableComponent("minetogether:gui.settings.button.disabled").withStyle(ChatFormatting.RED);
    }

    private void toggleEnabled() {
        Config config = Config.instance();
        if (config.chatEnabled) {
            MineTogetherChat.disableChat();
            config.chatEnabled = false;
        } else {
            MineTogetherChat.enableChat();
            config.chatEnabled = true;
        }
        Config.save();
    }

    private void setConfig(Runnable set) {
        set.run();
        Config.save();
    }

    private class BlockedEntry extends GuiElement<BlockedEntry> implements BackgroundRender {
        public BlockedEntry(@NotNull GuiParent<?> parent, Profile profile) {
            super(parent);
            this.constrain(HEIGHT, literal(14));

            GuiText name = new GuiText(this, TextComponent.EMPTY)
                    .setTextSupplier(() -> new TextComponent(FriendChatGui.displayName(profile)))
                    .setShadow(false)
                    .setAlignment(Align.LEFT)
                    .constrain(TOP, relative(get(TOP), 2))
                    .constrain(LEFT, relative(get(LEFT), 5))
                    .constrain(RIGHT, relative(get(RIGHT), -14))
                    .constrain(HEIGHT, literal(9));

            GuiButton unblock = MTStyle.Flat.button(this, (Supplier<Component>) null)
                    .setTooltip(new TranslatableComponent("minetogether:gui.settings.button.unblock.info"))
                    .setTooltipDelay(0)
                    .onPress(() -> {
                        profile.unmute();
                        updateBlockedList();
                    })
                    .constrain(TOP, match(get(TOP)))
                    .constrain(BOTTOM, match(get(BOTTOM)))
                    .constrain(RIGHT, match(get(RIGHT)))
                    .constrain(WIDTH, literal(14));

            GuiTexture removeTex = new GuiTexture(unblock, MTTextures.get("buttons/delete"));
            Constraints.bind(removeTex, unblock, 2);
        }

        @Override
        public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
            render.rect(getRectangle(), MTStyle.Flat.listEntryBackground(true));
        }
    }
}
