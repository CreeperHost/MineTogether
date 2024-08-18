package net.creeperhost.minetogether.chat.gui;

import net.covers1624.quack.collection.FastStream;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.gui.dialogs.GuiDialog;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcClient;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.creeperhost.polylib.client.modulargui.sprite.PolyTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.literal;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.relative;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 11/08/2024
 */
public class GroupChatElement extends GuiElement<GroupChatElement> implements BackgroundRender {

    public GroupChatElement(@NotNull GuiParent<?> parent) {
        super(parent);
        ProfileManager manager = MineTogetherChat.CHAT_STATE.profileManager;
        this.setEnabled(() -> manager.getPrivateGroup() != null);

        GuiButton select = new GuiButton(this)
                .onClick(FriendChatGui::selectGroupChat);
        Constraints.bind(select, this);

        GuiText name = new GuiText(this, TextComponent.EMPTY)
                .setTextSupplier(this::getGroupName)
                .setShadow(false)
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(get(TOP), 1))
                .constrain(LEFT, relative(get(LEFT), 3))
                .constrain(RIGHT, relative(get(RIGHT), -20))
                .constrain(HEIGHT, literal(9));

        GuiText players = new GuiText(this, TextComponent.EMPTY)
                .setTextSupplier(this::getUserCount)
                .setTooltip(this::getUserList)
                .setTooltipDelay(0)
                .setShadow(false)
                .setAlignment(Align.LEFT)
                .constrain(BOTTOM, relative(get(BOTTOM), -2))
                .constrain(LEFT, relative(get(LEFT), 3))
                .constrain(RIGHT, relative(get(RIGHT), -20))
                .constrain(HEIGHT, literal(9));

        GuiButton leave = MTStyle.Flat.button(this, TextComponent.EMPTY)
                .setTooltipSingle(this::getLeaveToolTip)
                .setTooltipDelay(0)
                .onPress(() -> {
                    ProfileManager.PrivateGroup group = manager.getPrivateGroup();
                    if (group == null) return;
                    GuiDialog.optionsDialog(getModularGui().getRoot(),
                            new TranslatableComponent(group.ownerHash == null ? "minetogether:gui.friends.group.confirm_leave_own" : "minetogether:gui.friends.group.confirm_leave"),
                            250,
                            GuiDialog.primary(new TranslatableComponent("gui.yes"), () -> {
                                manager.leaveGroup("leaving");
                                FriendChatGui.setSelected(null);
                            }),
                            GuiDialog.neutral(new TranslatableComponent("gui.cancel"), () -> {}));
                })
                .constrain(TOP, relative(get(TOP), 2))
                .constrain(RIGHT, relative(get(RIGHT), -2))
                .constrain(HEIGHT, literal(18))
                .constrain(WIDTH, literal(18));
        GuiTexture icon = new GuiTexture(leave, MTTextures.get("buttons/leave_group"));
        Constraints.bind(icon, leave);
    }

    private Component getGroupName() {
        ProfileManager manager = MineTogetherChat.CHAT_STATE.profileManager;
        ProfileManager.PrivateGroup group = manager.getPrivateGroup();
        if (group == null) {
            return TextComponent.EMPTY;
        }

        if (group.isOurGroup() || group.ownerHash == null) {
            return new TranslatableComponent("minetogether:gui.friends.group.own");
        } else {
            Profile profile = manager.lookupProfile(group.ownerHash);
            return new TranslatableComponent("minetogether:gui.friends.group.user", FriendChatGui.displayName(profile));
        }
    }

    private Component getUserCount() {
        ProfileManager manager = MineTogetherChat.CHAT_STATE.profileManager;
        ProfileManager.PrivateGroup group = manager.getPrivateGroup();
        if (group == null) {
            return TextComponent.EMPTY;
        }

        IrcClient client = MineTogetherChat.CHAT_STATE.ircClient;
        IrcChannel channel = client.getChannel(group.channelName);
        if (channel == null) {
            return TextComponent.EMPTY;
        }

        int count = channel.getUsers().size();
        return new TranslatableComponent("minetogether:gui.friends.group.user_count", count).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE);
    }

    private List<Component> getUserList() {
        ProfileManager manager = MineTogetherChat.CHAT_STATE.profileManager;
        ProfileManager.PrivateGroup group = manager.getPrivateGroup();
        if (group == null) {
            return Collections.emptyList();
        }

        IrcClient client = MineTogetherChat.CHAT_STATE.ircClient;
        IrcChannel channel = client.getChannel(group.channelName);
        if (channel == null) {
            return Collections.emptyList();
        }
        return FastStream.of(channel.getUsers())
                .sorted(Comparator.comparing(FriendChatGui::displayName))
                .map(profile -> {
                    String name = FriendChatGui.displayName(profile);
                    return (Component) new TextComponent(name).withStyle(profile == manager.getOwnProfile() ? ChatFormatting.GRAY : ChatFormatting.YELLOW);
                })
                .toList();
    }

    private Component getLeaveToolTip() {
        ProfileManager manager = MineTogetherChat.CHAT_STATE.profileManager;
        ProfileManager.PrivateGroup group = manager.getPrivateGroup();
        if (group == null) {
            return TextComponent.EMPTY;
        }

        return group.ownerHash == null ? new TranslatableComponent("minetogether:gui.friends.button.disband_group") : new TranslatableComponent("minetogether:gui.friends.button.leave_group");
    }

    @Override
    public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        render.rect(getRectangle(), MTStyle.Flat.listEntryBackground(isMouseOver() || FriendChatGui.groupChat));
    }
}
