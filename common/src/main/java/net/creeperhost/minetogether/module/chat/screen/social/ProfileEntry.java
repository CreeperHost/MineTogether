package net.creeperhost.minetogether.module.chat.screen.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import io.sentry.Sentry;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;
import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.minetogether.lib.chat.KnownUsers;
import net.creeperhost.minetogether.lib.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.polylib.client.screen.widget.buttons.ButtonString;
import net.creeperhost.polylib.client.toast.SimpleToast;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.io.IOException;
import java.util.List;

public class ProfileEntry extends Entry<ProfileEntry>
{
    private final Profile profile;
    private final List<GuiEventListener> children;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Button removeButton;
    private final Button addToPartyButton;

    private final Button muteButton;
    private final Button openDMButton;

    private final MineTogetherSocialInteractionsScreen mineTogetherSocialinteractionsScreen;

    public ProfileEntry(Profile profile, MineTogetherSocialInteractionsScreen mineTogetherSocialinteractionsScreen)
    {
        this.profile = profile;
        this.mineTogetherSocialinteractionsScreen = mineTogetherSocialinteractionsScreen;

        removeButton = new ButtonString(0, 0, 10, 10, Component.literal(ChatFormatting.RED + new String(Character.toChars(10006))), button ->
        {
            switch (this.mineTogetherSocialinteractionsScreen.getPage())
            {
                case BLOCKED:
                    ChatModule.unmuteUser(profile.getLongHash());
                    refreshPage();
                    break;
                case FRIENDS:
                    try
                    {
                        ChatCallbacks.removeFriend(profile.getFriendCode(), MineTogetherClient.getPlayerHash());
                    } catch (IOException e)
                    {
                        Sentry.captureException(e);
                    }
                    profile.setFriend(false);
                    KnownUsers.update(profile);
                    refreshPage();
                    break;
                case PARTY:
                    profile.setPartyMember(false);
                    KnownUsers.update(profile);
                    refreshPage();
                    break;
            }
        });

        addToPartyButton = new ButtonString(0, 0, 10, 10, Component.literal(ChatFormatting.GREEN + new String(Character.toChars(10010))), button ->
        {
            switch (this.mineTogetherSocialinteractionsScreen.getPage())
            {
                case FRIENDS:
                    String name = profile.isFriend() ? profile.getFriendName() : profile.getUserDisplay();

                    if (profile.isOnline())
                    {
                        SimpleToast simpleToast = new SimpleToast(Component.literal("Adding " + name + " to Party"), Component.literal(" "), Constants.MINETOGETHER_LOGO_LOCATION);
                        Minecraft.getInstance().getToasts().addToast(simpleToast);
//                        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("Adding " + name + " to Party"),
//                                mineTogetherSocialinteractionsScreen.width - 160, 0, 5000, ToastHandler.EnumToastType.DEFAULT, null);

                        ChatHandler.sendPartyInvite(profile.getMediumHash(), MineTogetherChat.profile.get().getMediumHash());
                    }
                    else
                    {
                        SimpleToast simpleToast = new SimpleToast(Component.literal("Unable to send invite      " + name + " is offline"), Component.literal(" "), Constants.MINETOGETHER_LOGO_LOCATION);
                        Minecraft.getInstance().getToasts().addToast(simpleToast);

//                        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("Unable to send invite      " + name + " is offline"),
//                                mineTogetherSocialinteractionsScreen.width - 160, 0, 5000, ToastHandler.EnumToastType.WARNING, null);
                    }
                    refreshPage();
                    break;
            }
        });

        this.muteButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, Constants.SOCIAL_INTERACTIONS_LOCATION, 256, 256, (button) ->
        {
            ChatModule.muteUser(profile.getLongHash());
            refreshPage();
        });

        this.openDMButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, Constants.SOCIAL_INTERACTIONS_LOCATION, 256, 256, (button) ->
        {
            if (profile != null)
            {
                if (profile.isOnline())
                {
                    Minecraft.getInstance().setScreen(new MineTogetherSocialChatScreen(mineTogetherSocialinteractionsScreen, profile));
                }
                else
                {
                    SimpleToast simpleToast = new SimpleToast(Component.literal("User is offline"), Component.literal(" "), Constants.MINETOGETHER_LOGO_LOCATION);
                    Minecraft.getInstance().getToasts().addToast(simpleToast);

//                    MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("User is offline"), 1000, ToastHandler.EnumToastType.WARNING, null);
                }
            }
        });

        this.children = ImmutableList.of(removeButton, addToPartyButton, muteButton, openDMButton);
    }

    public void refreshPage()
    {
        this.mineTogetherSocialinteractionsScreen.showPage(this.mineTogetherSocialinteractionsScreen.getPage());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f)
    {
        int p = k + 4;
        int q = j + (m - 24) / 2;
        int r = p + 24 + 4;
        Component component = Component.literal(profile.getUserDisplay());

        int t;
        if (component.equals(Component.empty()))
        {
            GuiComponent.fill(poseStack, k, j, k + l, j + m, FastColor.ARGB32.color(255, 74, 74, 74));
            t = j + (m - 9) / 2;
        }
        else
        {
            GuiComponent.fill(poseStack, k, j, k + l, j + m, FastColor.ARGB32.color(255, 48, 48, 48));
            t = j + (m - (9 + 9)) / 2;
            this.minecraft.font.draw(poseStack, component, (float) p, (float) (t + 12), FastColor.ARGB32.color(140, 255, 255, 255));
        }

        this.minecraft.font.draw(poseStack, profile.isFriend() ? profile.getFriendName() : profile.getUserDisplay(), (float) p, (float) t, FastColor.ARGB32.color(255, 255, 255, 255));

        if (this.mineTogetherSocialinteractionsScreen.getPage() == MineTogetherSocialInteractionsScreen.Page.FRIENDS)
        {
            this.addToPartyButton.x = k + (l - this.addToPartyButton.getWidth() - 4);
            this.addToPartyButton.y = j + ((m - this.addToPartyButton.getHeight()) / 2 + 10);
            this.addToPartyButton.render(poseStack, n, o, f);
            this.addToPartyButton.active = true;
        }
        else
        {
            this.addToPartyButton.x = 0;
            this.addToPartyButton.y = 0;
        }

        if (this.removeButton != null && this.mineTogetherSocialinteractionsScreen.getPage() != MineTogetherSocialInteractionsScreen.Page.ALL)
        {
            this.removeButton.x = k + (l - this.removeButton.getWidth() - 4);
            this.removeButton.y = j + ((m - this.removeButton.getHeight()) / 2 - 10);
            this.removeButton.render(poseStack, n, o, f);
            this.openDMButton.x = k + (l - this.openDMButton.getWidth() - 4 - 15);
            this.openDMButton.y = j + (m - this.openDMButton.getHeight()) / 2;
            this.openDMButton.render(poseStack, n, o, f);
        }
        else
        {
            if (this.removeButton != null)
            {
                this.removeButton.x = 0;
                this.removeButton.y = 0;
            }

            if (this.openDMButton != null && this.muteButton != null)
            {
                this.muteButton.x = k + (l - this.muteButton.getWidth() - 4) - 20;
                this.muteButton.y = j + (m - this.muteButton.getHeight()) / 2;
                if (profile.isMuted()) muteButton.active = false;
                this.muteButton.render(poseStack, n, o, f);
                this.openDMButton.x = k + (l - this.openDMButton.getWidth() - 4);
                this.openDMButton.y = j + (m - this.openDMButton.getHeight()) / 2;
                this.openDMButton.render(poseStack, n, o, f);
            }
        }
    }

    public void renderTooltips(PoseStack poseStack, int mouseX, int mouseY)
    {
        if (removeButton.isHoveredOrFocused())
        {
            Component component1 = switch (mineTogetherSocialinteractionsScreen.getPage()) {
                default -> Component.empty();
                case PARTY -> Component.literal("Remove from party");
                case FRIENDS -> Component.literal("Remove Friend");
                case BLOCKED -> Component.literal("Unblock");
            };
            mineTogetherSocialinteractionsScreen.renderTooltip(poseStack, component1, mouseX, mouseY);
        }

        if (addToPartyButton.isHoveredOrFocused())
            mineTogetherSocialinteractionsScreen.renderTooltip(poseStack, Component.literal("Add to party"), mouseX, mouseY);
        if (openDMButton.isHoveredOrFocused())
            mineTogetherSocialinteractionsScreen.renderTooltip(poseStack, Component.literal("Direct messages"), mouseX, mouseY);
        if (muteButton.isHoveredOrFocused())
            mineTogetherSocialinteractionsScreen.renderTooltip(poseStack, Component.literal("Block"), mouseX, mouseY);
    }

    public List<GuiEventListener> getChildren()
    {
        return children;
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return this.children;
    }


    public Profile getProfile()
    {
        return profile;
    }

    @Override
    public List<? extends NarratableEntry> narratables()
    {
        return null;
    }
}
