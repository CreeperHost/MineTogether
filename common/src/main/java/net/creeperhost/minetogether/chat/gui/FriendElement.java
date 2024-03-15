package net.creeperhost.minetogether.chat.gui;

import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.gui.dialogs.ContextMenu;
import net.creeperhost.minetogether.gui.dialogs.TextInputDialog;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.util.ProfileUpdater;
import net.creeperhost.polylib.client.modulargui.elements.GuiButton;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.elements.GuiText;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 30/09/2023
 */
class FriendElement extends GuiElement<FriendElement> implements BackgroundRender {

    @Nullable
    private final ProfileManager.FriendRequest request;
    @Nullable
    private final Profile profile;
    @Nullable
    private GameProfile iconProfile = null;

    private PlayerIconElement icon;

    public FriendElement(@NotNull GuiParent<?> parent, @Nullable Profile profile) {
        this(parent, null, profile);
    }

    public FriendElement(@NotNull GuiParent<?> parent, ProfileManager.FriendRequest request) {
        this(parent, request, request.user);
    }

    public FriendElement(@NotNull GuiParent<?> parent, @Nullable ProfileManager.FriendRequest request, @Nullable Profile profile) {
        super(parent);
        this.request = request;
        this.profile = profile;
        this.constrain(HEIGHT, literal(profile == null ? 12 : 32));

        if (profile == null) return;

        icon = new PlayerIconElement(this, null)
                .constrain(TOP, relative(get(TOP), 2))
                .constrain(LEFT, relative(get(LEFT), 2))
                .constrain(WIDTH, literal(28))
                .constrain(HEIGHT, literal(28));

        icon.setTooltip(() -> {
            if (!profile.isOnline()) {
                return Collections.singletonList(Component.translatable("minetogether:gui.friends.icon.offline"));
            } else if (!profile.hasFriendUUID()) {
                return Collections.singletonList(Component.translatable("minetogether:gui.friends.icon.no_uuid"));
            } else if (icon.textureFail) {
                return Collections.singletonList(Component.translatable("minetogether:gui.friends.icon.fail"));
            }
            return Collections.emptyList();
        }, 0);

        GuiText name = new GuiText(this, Component.empty())
                .setTextSupplier(() -> Component.literal(FriendChatGui.displayName(profile)))
                .setShadow(false)
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(get(TOP), 5))
                .constrain(LEFT, relative(icon.get(RIGHT), 3))
                .constrain(RIGHT, relative(get(RIGHT), -2))
                .constrain(HEIGHT, literal(9));

        if (request == null) {
            GuiText status = new GuiText(this, Component.empty())
                    .setShadow(false)
                    .setTextSupplier(() -> profile.isOnline() ? Component.translatable("minetogether:gui.friends.online").withStyle(ChatFormatting.DARK_GREEN) : Component.translatable("minetogether:gui.friends.offline"))
                    .setAlignment(Align.LEFT)
                    .constrain(BOTTOM, relative(get(BOTTOM), -5))
                    .constrain(LEFT, relative(icon.get(RIGHT), 3))
                    .constrain(RIGHT, relative(get(RIGHT), -2))
                    .constrain(HEIGHT, literal(9));
        } else {
            GuiButton accept = MTStyle.Flat.buttonPrimary(this, Component.translatable("minetogether:gui.friends.button.accept"))
                    .onPress(() -> {
                        ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
                        new TextInputDialog(getModularGui().getRoot(), Component.translatable("minetogether:screen.friendreq.desc.request"), FriendChatGui.displayName(profile))
                                .setResultCallback(friendName -> profileManager.acceptFriendRequest(request, friendName.trim()));
                    })
                    .constrain(BOTTOM, relative(get(BOTTOM), -2))
                    .constrain(LEFT, relative(get(LEFT), 34))
                    .constrain(HEIGHT, literal(14));

            GuiButton reject = MTStyle.Flat.buttonCaution(this, Component.translatable("minetogether:gui.friends.button.reject"))
                    .onPress(() -> MineTogetherChat.CHAT_STATE.profileManager.denyFriendRequest(request))
                    .constrain(BOTTOM, relative(get(BOTTOM), -2))
                    .constrain(HEIGHT, literal(14))
                    .constrain(RIGHT, relative(get(RIGHT), -2));

            accept.constrain(RIGHT, midPoint(accept.get(LEFT), reject.get(RIGHT), -0.5));
            reject.constrain(LEFT, midPoint(accept.get(LEFT), reject.get(RIGHT), 0.5));

        }
    }

    @Override
    public void tick(double mouseX, double mouseY) {
        super.tick(mouseX, mouseY);

        if (profile != null && profile.hasFriendUUID() && iconProfile == null) {
            iconProfile = new GameProfile(profile.getFriendUUID(), "");
            ProfileUpdater.updateProfile(profile.getFriendUUID(), e -> {
                iconProfile = e;
                icon.setProfile(e);
            });
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (profile == null || !isMouseOver()) return false;

        if (button == GuiButton.LEFT_CLICK) {
            if (request == null) {
                if (FriendChatGui.selected == profile) {
                    showOptions(mouseX, mouseY);
                } else {
                    FriendChatGui.selected = profile;
                }
                return true;
            }
        } else if (button == GuiButton.RIGHT_CLICK) {
            showOptions(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private void showOptions(double mouseX, double mouseY) {
        if (profile == null) return;
        ContextMenu menu = new ContextMenu(getModularGui().getRoot());
        menu.addTitle(Component.literal(FriendChatGui.displayName(profile)).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD));

        if (request == null) {
            menu.addOption(Component.translatable("minetogether:gui.friends.button.rename").withStyle(ChatFormatting.AQUA), () -> {
                ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
                new TextInputDialog(getModularGui().getRoot(), Component.translatable("minetogether:screen.friendreq.desc.request"), FriendChatGui.displayName(profile))
                        .setResultCallback(friendName -> {
                            profileManager.updateFriendName(profile, friendName.trim(), success -> {
                                MineTogetherChat.simpleToast(Component.translatable(success ? "minetogether:gui.friends.update_friend" : "minetogether:gui.friends.update_fail"));
                            });
                        });
            });
//                menu.addOption(Component.translatable("minetogether:gui.friends.button.party").withStyle(ChatFormatting.AQUA), () -> {});
            menu.addOption(Component.translatable("minetogether:gui.friends.button.remove").withStyle(ChatFormatting.YELLOW), () -> {
                MineTogetherChat.CHAT_STATE.profileManager.removeFriend(profile);
                FriendChatGui.selected = null;
            });
        }

        menu.addOption(Component.translatable("minetogether:gui.friends.button.block").withStyle(ChatFormatting.RED), () -> {
            profile.mute();
            FriendChatGui.selected = null;
        });
        menu.setPosition(mouseX, mouseY);
    }

    @Override
    public double getBackgroundDepth() {
        return BackgroundRender.super.getBackgroundDepth();
    }

    @Override
    public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        if (profile == null) {
            render.drawCenteredString(Component.translatable("minetogether:gui.friends.requests").withStyle(ChatFormatting.UNDERLINE), xCenter(), yMin() + 2, 0xFFFFFF, false);
            return;
        }
        render.rect(getRectangle(), MTStyle.Flat.listEntryBackground((isMouseOver() && request == null) || FriendChatGui.selected == profile));
    }
}
