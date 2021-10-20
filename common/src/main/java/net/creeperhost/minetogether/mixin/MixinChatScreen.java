package net.creeperhost.minetogether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;
import net.creeperhost.minetogether.lib.chat.ChatConnectionStatus;
import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.minetogether.lib.chat.KnownUsers;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.lib.chat.irc.IrcHandler;
import net.creeperhost.minetogether.lib.util.MathHelper;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.ClientChatTarget;
import net.creeperhost.minetogether.module.chat.screen.FriendRequestScreen;
import net.creeperhost.minetogether.module.chat.screen.widgets.GuiButtonPair;
import net.creeperhost.minetogether.util.ComponentUtils;
import net.creeperhost.minetogethergui.gif.AnimatedGif;
import net.creeperhost.minetogethergui.widgets.ButtonNoBlend;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen
{
    @Shadow
    public abstract void render(PoseStack poseStack, int i, int j, float f);

    @Shadow
    protected EditBox input;
    @Shadow
    private CommandSuggestions commandSuggestions;
    @Shadow
    private String initial;
    private GuiButtonPair switchButton;
    private DropdownButton<net.creeperhost.minetogether.module.chat.screen.ChatScreen.Menu> dropdownButton;
    private String currentDropdown;
    private int mouseX;
    private int mouseY;
    private Button newUserButton;
    private Button disableButton;
    private AnimatedGif gifImage;
    private AnimatedGif.GifPlayer gifPlayer;

    protected MixinChatScreen(Component component)
    {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    public void init(CallbackInfo ci)
    {
        if (!Config.getInstance().isChatEnabled()) return;

        if(ChatModule.clientChatTarget == ClientChatTarget.MINETOGETHER)
        {
            commandSuggestions.setAllowSuggestions(false);
        }

        if (initial.equalsIgnoreCase("/"))
        {
            if (ChatModule.clientChatTarget != ClientChatTarget.DEFAULT) ChatModule.lastSelected = ChatModule.clientChatTarget;
            ChatModule.clientChatTarget = ClientChatTarget.DEFAULT;
        }
        else if (ChatModule.lastSelected != ClientChatTarget.DEFAULT)
        {
            ChatModule.clientChatTarget = ChatModule.lastSelected;
        }

        int x = MathHelper.ceil(((float) Minecraft.getInstance().gui.getChat().getWidth())) + 16 + 2;

        if (ChatHandler.hasParty)
        {
            addButton(switchButton = new GuiButtonPair(x, height - 215, 234, 16, ChatModule.clientChatTarget == ClientChatTarget.MINETOGETHER ? 1 : 0, false, false, true, p ->
            {
                if (switchButton.activeButton == 2)
                {
                    ChatModule.clientChatTarget = ClientChatTarget.PARTY;
                    return;
                }
                ChatModule.clientChatTarget = switchButton.activeButton == 1 ? ClientChatTarget.MINETOGETHER : ClientChatTarget.DEFAULT;

            }, isSinglePlayer() ? I18n.get("minetogether.ingame.chat.local") : I18n.get("minetogether.ingame.chat.server"), I18n.get("minetogether.ingame.chat.global"), I18n.get("minetogether.ingame.chat.party")));
        }
        else
        {
            addButton(switchButton = new GuiButtonPair(x, height - 215, 234, 16, ChatModule.clientChatTarget == ClientChatTarget.MINETOGETHER ? 1 : 0, false, false, true, p ->
            {
                ChatModule.clientChatTarget = switchButton.activeButton == 1 ? ClientChatTarget.MINETOGETHER : ClientChatTarget.DEFAULT;
                if (switchButton.activeButton == 1) IrcHandler.sendCTCPMessage("Freddy", "ACTIVE", "");

            }, isSinglePlayer() ? I18n.get("minetogether.ingame.chat.local") : I18n.get("minetogether.ingame.chat.server"), I18n.get("minetogether.ingame.chat.global")));
        }

        List<String> strings = new ArrayList<>();

        strings.add(I18n.get("minetogether.chat.button.mute"));
        strings.add(I18n.get("minetogether.chat.button.addfriend"));
        strings.add(I18n.get("minetogether.chat.button.mention"));

        addButton(dropdownButton = new DropdownButton<>(-1000, -1000, 100, 20, new TranslatableComponent("Menu"), new net.creeperhost.minetogether.module.chat.screen.ChatScreen.Menu(strings), true, p ->
        {
            if (dropdownButton.getSelected().option.equals(I18n.get("minetogether.chat.button.mute")))
            {
                ChatModule.muteUser(KnownUsers.findByDisplay(currentDropdown).getLongHash());
                KnownUsers.findByDisplay(currentDropdown).setMuted(true);
                ChatHandler.addStatusMessage("Locally blocked " + currentDropdown);
            }
            else if (dropdownButton.getSelected().option.equals(I18n.get("minetogether.chat.button.addfriend")))
            {
                Profile profile = KnownUsers.findByDisplay(currentDropdown);
                if (profile != null)
                    minecraft.setScreen(new FriendRequestScreen(this, minecraft.getUser().getName(), profile, ChatCallbacks.getFriendCode(MineTogetherClient.getPlayerHash()), "", false, false));
            }
            else if (dropdownButton.getSelected().option.equals(I18n.get("minetogether.chat.button.mention")))
            {
                input.setFocus(true);
                input.setValue(input.getValue() + " " + currentDropdown + " ");
            }
        }));
        dropdownButton.flipped = true;
        if (Config.getInstance().getFirstConnect() && ChatModule.clientChatTarget == ClientChatTarget.MINETOGETHER)
        {
            ChatCallbacks.updateOnlineCount();

            addButton(newUserButton = new ButtonNoBlend(6, height - ((minecraft.gui.getChat().getHeight() + 80) / 2) + 45, minecraft.gui.getChat().getWidth() - 2, 20, new TranslatableComponent("Join " + ChatCallbacks.onlineCount + " online users now!"), p ->
            {
                IrcHandler.sendCTCPMessage("Freddy", "ACTIVE", "");
                Config.getInstance().setFirstConnect(false);
                newUserButton.visible = false;
                disableButton.visible = false;
                minecraft.setScreen(null);
            }));
            addButton(disableButton = new ButtonNoBlend(6, height - ((minecraft.gui.getChat().getHeight() + 80) / 2) + 70, minecraft.gui.getChat().getWidth() - 2, 20, new TranslatableComponent("Don't ask me again"), p ->
            {
                Config.getInstance().setChatEnabled(false);
                disableButton.visible = false;
                newUserButton.visible = false;
                IrcHandler.stop(true);
                buttons.clear();
            }));
        }
    }

    private static boolean isSinglePlayer()
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSingleplayerServer() == null) return false;
        if (minecraft.getSingleplayerServer().isPublished()) return false;
        if (minecraft.isLocalServer()) return true;

        return false;
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(PoseStack poseStack, int i, int j, float partialTicks, CallbackInfo ci)
    {
        if (!Config.getInstance().isChatEnabled()) return;

        //This is just to stop IntelliJ from complaining
        if (minecraft == null) return;

        mouseX = i;
        mouseY = j;

        setFocused(input);
        input.setFocus(true);
        fill(poseStack, 2, this.height - 14, this.width - 2, this.height - 2, minecraft.options.getBackgroundColor(-2147483648));

        input.render(poseStack, i, j, partialTicks);
        if (ChatModule.clientChatTarget == ClientChatTarget.DEFAULT) commandSuggestions.render(poseStack, i, j);
        Style style = minecraft.gui.getChat().getClickedComponentStyleAt((double) i, (double) j);
        if (style != null && style.getHoverEvent() != null)
        {
            if (style.getHoverEvent().getAction() == ComponentUtils.RENDER_GIF)
            {
                Component urlComponent = (Component) style.getHoverEvent().getValue(ComponentUtils.RENDER_GIF);
                String url = urlComponent.getString();
                if (gifImage == null)
                {
                    try
                    {
                        try
                        {
                            gifImage = AnimatedGif.fromURL(new URL("https://ss.gigabit101.net/Colony_Survival.png"));
                        } catch (IOException ignored)
                        {
                        }
                        if (gifImage != null && gifPlayer == null)
                        {
                            gifPlayer = gifImage.makeGifPlayer();
                            gifPlayer.setLooping(true);
                            gifPlayer.setAutoplay(true);
                        }
                    } catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
                if (gifPlayer != null) gifPlayer.render(poseStack, mouseX + 5, mouseY + 5, 80, 60, partialTicks);
            }
            this.renderComponentHoverEffect(poseStack, style, i, j);
        }
        else
        {
            gifImage = null;
            gifPlayer = null;
        }
        if (Config.getInstance().getFirstConnect() && ChatModule.clientChatTarget == ClientChatTarget.MINETOGETHER)
        {
            if (newUserButton != null) newUserButton.visible = true;
            if (disableButton != null) disableButton.visible = true;

            ChatComponent chatComponent = minecraft.gui.getChat();
            if (chatComponent != null)
            {
                int y = height - 43 - (minecraft.font.lineHeight * Math.max(Math.min(chatComponent.getRecentChat().size(), chatComponent.getLinesPerPage()), 20));
                fill(poseStack, 0, y, chatComponent.getWidth() + 6, chatComponent.getHeight() + 10 + y, 0x99000000);

                drawCenteredString(poseStack, font, "Welcome to MineTogether", (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2), 0xFFFFFF);
                drawCenteredString(poseStack, font, "MineTogether is a multiplayer enhancement mod that provides", (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 10, 0xFFFFFF);
                drawCenteredString(poseStack, font, "a multitude of features like chat, friends list, server listing", (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 20, 0xFFFFFF);
                drawCenteredString(poseStack, font, "and more. Join " + ChatCallbacks.userCount + " unique users.", (chatComponent.getWidth() / 2) + 3, height - ((chatComponent.getHeight() + 80) / 2) + 30, 0xFFFFFF);
            }
        }
        super.render(poseStack, i, j, partialTicks);
        ci.cancel();
    }

    /*
     * Used to update suggestions
     */
    @Inject(at = @At("TAIL"), method = "tick")
    public void tick(CallbackInfo ci)
    {
        if (!Config.getInstance().isChatEnabled()) return;

        switch (ChatModule.clientChatTarget)
        {
            case DEFAULT:
                switchButton.getButtons().get(0).setActive(true);
                return;
            case MINETOGETHER:
                switchButton.getButtons().get(1).setActive(true);
                return;
            case PARTY:
                if (switchButton.getButtons().size() == 3)
                {
                    switchButton.getButtons().get(2).setActive(true);
                }
                return;
        }

        if (gifPlayer != null) gifPlayer.tick();

        //This should never happen but better safe than sorry
        if (input == null) return;

        if (ChatModule.clientChatTarget != ClientChatTarget.DEFAULT)
        {
            input.active = ChatHandler.connectionStatus == ChatConnectionStatus.VERIFIED;
            input.setEditable(ChatHandler.connectionStatus == ChatConnectionStatus.VERIFIED);
            //Remove focus if the client is not verified
            if (input.isFocused() && ChatHandler.connectionStatus != ChatConnectionStatus.VERIFIED)
            {
                input.setFocus(false);
            }
            switch (ChatHandler.connectionStatus)
            {
                case VERIFYING:
                    input.setSuggestion(I18n.get("minetogether.chat.message.unverified"));
                    break;
                case BANNED:
                    input.setSuggestion(I18n.get("minetogether.chat.message.banned"));
                    break;
                case DISCONNECTED:
                    input.setSuggestion(I18n.get("minetogether.chat.message.disconnect"));
                    break;
                case CONNECTING:
                    input.setSuggestion(I18n.get("minetogether.chat.message.connecting"));
                    break;
                case VERIFIED:
                    input.setSuggestion("");
                    break;
            }
        }
        else
        {
            //Set these back when the tab is switched
            input.active = true;
            input.setEditable(true);
            input.setSuggestion("");
        }
    }

    /*
     * Used to remove any left over open dropdowns, Called at the tail of mouseClicked to avoid it interfering with handleComponentClicked
     */
    @Inject(at = @At("TAIL"), method = "mouseClicked", cancellable = true)
    public void mouseClicked(double d, double e, int i, CallbackInfoReturnable<Boolean> cir)
    {
        if (!Config.getInstance().isChatEnabled()) return;

        if (dropdownButton != null && dropdownButton.wasJustClosed && !dropdownButton.dropdownOpen)
        {
            dropdownButton.x = dropdownButton.y = -10000;
            dropdownButton.wasJustClosed = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "onEdited", cancellable = true)
    private void onEdited(String string, CallbackInfo ci)
    {
        if(ChatModule.clientChatTarget == ClientChatTarget.MINETOGETHER)
        {
            ci.cancel();
        }
    }

    @Override
    public void sendMessage(String string)
    {
        if (!Config.getInstance().isChatEnabled()) return;

        //This is just to stop IntelliJ from complaining
        if (minecraft == null) return;

        //If it's our chat screen send the message to our chat handler for sending
        if (ChatModule.clientChatTarget == ClientChatTarget.MINETOGETHER)
        {
            //Force commands to the "default" tab
            if(string.startsWith("/"))
            {
                ChatModule.clientChatTarget = ClientChatTarget.DEFAULT;
                super.sendMessage(string);
                return;
            }
            ChatHandler.sendMessage(ChatHandler.CHANNEL, string);
            return;
        }
        if (ChatModule.clientChatTarget == ClientChatTarget.PARTY)
        {
            ChatHandler.sendMessage(ChatHandler.currentParty, string);
            return;
        }
        super.sendMessage(string);
    }

    @Override
    public boolean handleComponentClicked(@Nullable Style style)
    {
        if (!Config.getInstance().isChatEnabled()) return false;

        //This is just to stop IntelliJ from complaining
        if (minecraft == null) return false;
        //Let vanilla take over when its not using our tab
        if (ChatModule.clientChatTarget == ClientChatTarget.DEFAULT) return super.handleComponentClicked(style);
        //If the Style is null there is nothing to be done
        if (style == null) return false;
        //If the click event is null there is nothing to be done
        if (style.getClickEvent() == null) return false;
        //This should never be null but lets be safe
        if (dropdownButton == null) return false;
        //If the dropdown is already open lets not do anything or this could lead to issues
        if (dropdownButton.dropdownOpen) return false;
        //Still allow openurl click event
        if (style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) return super.handleComponentClicked(style);

        //Don't bother with suggestions in our tab as we replace it with the dropdown
        if (style.getClickEvent().getAction() == ClickEvent.Action.SUGGEST_COMMAND)
        {
            dropdownButton.x = mouseX;
            dropdownButton.y = mouseY;
            dropdownButton.dropdownOpen = true;
            currentDropdown = style.getClickEvent().getValue();
            return true;
        }
        return false;
    }
}
