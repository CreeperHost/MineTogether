package net.creeperhost.minetogether.client.screen.serverlist.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.client.screen.GDPRScreen;
import net.creeperhost.minetogether.client.screen.element.ButtonString;
import net.creeperhost.minetogether.client.screen.list.GuiList;
import net.creeperhost.minetogether.client.screen.list.GuiListEntryFriend;
import net.creeperhost.minetogether.client.screen.list.GuiListEntryMuted;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.data.FriendStatusResponse;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class FriendsListScreen extends Screen
{
    private final Screen parent;
    private GuiList<GuiListEntryFriend> list;
    private GuiList<GuiListEntryMuted> listMuted;
    
    private Button buttonAdd;
    private Button buttonCancel;
    private Button buttonInvite;
    private Button buttonCopy;
    private Button buttonRefresh;
    private Button buttonChat;
    private Button buttonRemove;
    private Button toggle;
    private Button channelInviteButton;
    private TextFieldWidget codeEntry;
    private TextFieldWidget displayEntry;
    private TextFieldWidget searchEntry;
    
    private boolean addFriend = false;
    private String friendCode;
    private boolean first = true;
    private String friendDisplayString;
    private String errorText = null;
    private String hoveringText = null;
    private String lastHoveringText = null;
    private ArrayList<IReorderingProcessor> hoverTextCache = null;
    private Friend removeFriend;
    private String unmutePlayer;
    private Friend invitedPlayer;
    private boolean channelInvite = false;
    private boolean isMuted = false;
    
    public FriendsListScreen(Screen currentScreen)
    {
        super(new StringTextComponent(""));
        this.parent = currentScreen;
        friendCode = Callbacks.getFriendCode();
        MineTogether.instance.toastHandler.clearToast(false);
    }
    
    @Override
    public void init()
    {
        if (!MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            minecraft.displayGuiScreen(new GDPRScreen(parent, () -> new FriendsListScreen(parent)));
            return;
        }
        super.init();
        
        if (listMuted == null)
        {
            listMuted = new GuiList(this, minecraft, width, height, 32, this.height - 64, 36);
        } else
        {
            listMuted.updateSize(width, height, 32, this.height - 64);
        }
        
        if (list == null)
        {
            list = new GuiList(this, minecraft, width, height, 32, this.height - 64, 36);
        } else
        {
            list.updateSize(width, height, 32, this.height - 64);
        }
        
        if (first)
        {
            first = false;
            refreshFriendsList(true);
            refreshMutedList(true);
        }
        
        int y = this.height - 60;
        
        int margin = 10;
        int buttons = 3;
        int buttonWidth = 80;
        
        int totalButtonSize = (buttonWidth * buttons);
        int nonButtonSpace = (width - (margin * 2)) - totalButtonSize;
        
        int spaceInbetween = (nonButtonSpace / (buttons - 1)) + buttonWidth;
        
        int buttonX = margin;
        
        buttonCancel = addButton(new Button(buttonX, y, buttonWidth, 20, new StringTextComponent(Util.localize("button.cancel")), p ->
        {
            if (!addFriend)
                minecraft.displayGuiScreen(parent);
            else
            {
                addFriend = false;
                buttonInvite.visible = true;
                codeEntry.setText("");
            }
        }));
        buttonX += spaceInbetween;
        
        buttonAdd = addButton(new Button(buttonX, y, buttonWidth, 20, new StringTextComponent(Util.localize("multiplayer.button.addfriend")), p ->
        {
            if (!addFriend)
            {
                addFriend = true;
                buttonInvite.visible = false;
            } else if (!codeEntry.getText().isEmpty())
            {
                AtomicReference<FriendStatusResponse> result = new AtomicReference<>(null);
                addFriend = false;

                CompletableFuture.runAsync(() ->
                {
                    result.set(Callbacks.addFriend(codeEntry.getText(), displayEntry.getText()));
                    if (result.get() != null) {
                        Profile profile = ChatHandler.knownUsers.findByHash(result.get().getHash());
                        if (profile == null) ChatHandler.knownUsers.add(result.get().getHash());
                        if (profile != null) list.add(new GuiListEntryFriend(this, list, new Friend(displayEntry.getText(), codeEntry.getText(), false)));
                    }
                }, MineTogether.profileExecutor);

                buttonInvite.visible = true;
                showAlert(result.get() == null || result.get().getMessage().isEmpty() ? new StringTextComponent(Util.localize("multiplayer.friendsent")) : new StringTextComponent(result.get().getMessage()), 0x00FF00, 5000);
            }
        }));
        buttonX += spaceInbetween;
        
        buttonInvite = addButton(new Button(buttonX, y, buttonWidth, 20, new StringTextComponent(Util.localize("multiplayer.button.invite")), p ->
        {
            if (MineTogether.instance.curServerId == -1)
            {
                showAlert(new StringTextComponent(Util.localize("multiplayer.notinvite")), 0xFF0000, 5000);
                return;
            } else
            {
                boolean ret = Callbacks.inviteFriend(list.getCurrSelected().getFriend());
                if (ret)
                {
                    Callbacks.inviteFriend(list.getCurrSelected().getFriend());
                    showAlert(new StringTextComponent(Util.localize("multiplayer.invitesent")), 0x00FF00, 5000);
                } else
                {
                    showAlert(new StringTextComponent(Util.localize("multiplayer.couldnotinvite")), 0xFF0000, 5000);
                }
            }
        }));
        
        buttonInvite.active = list.getSelected() != null;
        
        codeEntry = new TextFieldWidget(font, this.width / 2 - 80, this.height / 2 - 50, 160, 20, new StringTextComponent(""));
        displayEntry = new TextFieldWidget(font, this.width / 2 - 80, this.height / 2, 160, 20, new StringTextComponent(""));
        
        buttonRefresh = addButton(new Button(this.width - 90, this.height - 26, 80, 20, new StringTextComponent(Util.localize("multiplayer.button.refresh")), p ->
        {
            refreshFriendsList(false);
            refreshMutedList(false);
        }));

        addButton(buttonCopy = new ButtonString( 5, this.height - 26, 60, 20, MineTogether.profile.get().getFriendCode(), p ->
        {
            this.minecraft.keyboardListener.setClipboardString(MineTogether.profile.get().getFriendCode());
            showAlert(new StringTextComponent("Copied to clipboard."), 0x00FF00, 5000);
        }));

        toggle = addButton(new Button(width - 60, 6, 60, 20, isMuted ? new StringTextComponent("Friends") : new StringTextComponent("Muted"), p ->
        {
            if (toggle.getMessage().getString().contains("Muted"))
            {
                toggle.setMessage(new StringTextComponent("Friends"));
                isMuted = true;
            } else if (toggle.getMessage().getString().contains("Friends"))
            {
                toggle.setMessage(new StringTextComponent("Muted"));
                isMuted = false;
            }
        }));
        
        searchEntry = new TextFieldWidget(this.font, this.width / 2 - 80, y + 28, 160, 20, new StringTextComponent(""));
    }
    public static ArrayList<String> removedFriends = new ArrayList<>();

    protected void refreshFriendsList(boolean force)
    {
        ArrayList<Friend> friends = Callbacks.getFriendsList(force);
        list.clearList();
        if (friends != null)
        {
            for (Friend friend : friends)
            {
                GuiListEntryFriend friendEntry = new GuiListEntryFriend(this, list, friend);
                if(searchEntry != null && !searchEntry.getText().isEmpty())
                {
                    String s = searchEntry.getText();
                    if(friend.getName().toLowerCase().contains(s.toLowerCase()))
                    {
                        if(!removedFriends.contains(friend.getCode())) list.add(friendEntry);
                    }
                }
                else
                {
                    if(!removedFriends.contains(friend.getCode())) list.add(friendEntry);
                }
            }
            ArrayList<String> removedCopy = new ArrayList<String>(removedFriends);
            for(String removed : removedCopy)
            {
                boolean isInList = false;
                for(Friend friend : friends)
                {
                    if(friend.getCode().equalsIgnoreCase(removed))
                    {
                        isInList=true;
                        break;
                    }
                }
                if(!isInList)
                {
                    removedFriends.remove(removed);
                }
            }
        }
    }
    
    protected void refreshMutedList(boolean force)
    {
        ArrayList<String> mutedUsers = MineTogether.mutedUsers;
        listMuted.clearList();
        if (mutedUsers != null)
        {
            for (String mute : mutedUsers)
            {
                String username = MineTogether.instance.getNameForUser(mute);
                GuiListEntryMuted mutedEntry = new GuiListEntryMuted(this, listMuted, username);
                if (searchEntry != null && !searchEntry.getText().isEmpty())
                {
                    String s = searchEntry.getText();
                    if (mute.toLowerCase().contains(s.toLowerCase()))
                    {
                        listMuted.add(mutedEntry);
                    }
                } else
                {
                    listMuted.add(mutedEntry);
                }
            }
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void onClose()
    {
        MineTogether.instance.toastHandler.clearToast(false);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(0);
        if (!isMuted)
        {
            this.searchEntry.setVisible(true);

            if (!addFriend)
            {
                this.list.render(matrixStack, mouseX, mouseY, partialTicks);
            } else
            {
                this.drawCenteredString(matrixStack, this.font, Util.localize("multiplayer.othercode"), this.width / 2, this.height / 2 - 60, 0xFFFFFF);
                this.drawCenteredString(matrixStack, this.font, Util.localize("multiplayer.displayname"), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
                this.codeEntry.render(matrixStack, mouseX, mouseY, partialTicks);
                this.displayEntry.render(matrixStack, mouseX, mouseY, partialTicks);
                this.searchEntry.setVisible(false);
            }
            this.drawCenteredString(matrixStack, this.font, Util.localize("multiplayer.friends"), this.width / 2, 10, -1);
        } else
        {
            this.listMuted.render(matrixStack, mouseX, mouseY, partialTicks);
            this.drawCenteredString(matrixStack, this.font, Util.localize("multiplayer.muted"), this.width / 2, 10, -1);
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        
        if (hoveringText != null)
        {
            if (!hoveringText.equals(lastHoveringText))
            {
                hoverTextCache = new ArrayList<>();
                hoverTextCache.add(new StringTextComponent(hoveringText).func_241878_f());
                lastHoveringText = hoveringText;
            }
            renderTooltip(matrixStack, hoverTextCache, mouseX, mouseY);
        }
        if (searchEntry != null) this.searchEntry.render(matrixStack, mouseX, mouseY, partialTicks);

        this.minecraft.fontRenderer.drawStringWithShadow(matrixStack, I18n.format("creeperhost.multiplayer.friendcode"), 10, this.height - 35, -1);
    }
    
    @Override
    public void tick()
    {
        super.tick();
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if (codeEntry.isFocused())
        {
            codeEntry.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
            return true;
        } else if (displayEntry.isFocused())
        {
            displayEntry.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
            return true;
        } else if (searchEntry.isFocused())
        {
            searchEntry.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
            refreshFriendsList(false);
            return true;
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode)
    {
        if (codeEntry.isFocused())
        {
            codeEntry.charTyped(typedChar, keyCode);
            return true;
        } else if (displayEntry.isFocused())
        {
            displayEntry.charTyped(typedChar, keyCode);
            return true;
        } else if (searchEntry.isFocused())
        {
            searchEntry.charTyped(typedChar, keyCode);
            refreshFriendsList(false);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }
    
    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        list.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return false;
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        this.codeEntry.mouseClicked(mouseX, mouseY, mouseButton);
        this.displayEntry.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!isMuted)
        {
            this.list.mouseClicked(mouseX, mouseY, mouseButton);
        } else
        {
            this.listMuted.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (list.getSelected() != null)
            if (((GuiListEntryFriend) list.getSelected()).getFriend().isAccepted())
                this.buttonInvite.active = true;
            else
                this.buttonInvite.active = false;
        else
            this.buttonInvite.active = false;
        
        this.searchEntry.mouseClicked(mouseX, mouseY, mouseButton);
        return true;
    }
    
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        if (isMuted)
        {
            this.list.mouseReleased(mouseX, mouseY, state);
        } else
        {
            this.listMuted.mouseReleased(mouseX, mouseY, state);
        }
        return true;
    }
    
    private void showAlert(ITextComponent text, int colour, int time)
    {
        MineTogether.instance.toastHandler.displayToast(text, time, null);
    }
    
    public void setHoveringText(String hoveringText)
    {
        this.hoveringText = hoveringText;
    }
    
    BooleanConsumer removeConsumer = new BooleanConsumer()
    {
        @Override
        public void accept(boolean t)
        {
            if (t)
            {
                CompletableFuture.runAsync(() -> {
                    removedFriends.add(removeFriend.getCode());
                    refreshFriendsList(true);
                    if(!Callbacks.removeFriend(removeFriend.getCode()))
                    {
                        removedFriends.remove(removeFriend.getCode());
                        refreshFriendsList(true);
                    }
                });
            }
            minecraft.displayGuiScreen(new FriendsListScreen(parent));
        }
    };
    
    public void removeFriend(Friend friend)
    {
        removeFriend = friend;
        minecraft.displayGuiScreen(new ConfirmScreen(removeConsumer, new StringTextComponent(I18n.format("minetogether.removefriend.sure1")), new StringTextComponent(I18n.format("minetogether.removefriend.sure2"))));
    }
    
    BooleanConsumer invitedConsumer = new BooleanConsumer()
    {
        @Override
        public void accept(boolean t)
        {
            if (t)
            {
                if (!invitedPlayer.isAccepted())
                    showAlert(new StringTextComponent("Cannot invite pending friends"), 0x00FF00, 5000);
                else
                {
                    String friendCode = "MT" + invitedPlayer.getCode().substring(0, 28);
                    showAlert(new StringTextComponent("Sent invite to " + invitedPlayer.getName()), 0x00FF00, 5000);
                    ChatHandler.sendChannelInvite(friendCode, MineTogether.instance.ourNick);
                }
            }
            minecraft.displayGuiScreen(new FriendsListScreen(parent));
        }
    };
    
    public void inviteGroupChat(Friend invited)
    {
        invitedPlayer = invited;
        minecraft.displayGuiScreen(new ConfirmScreen(invitedConsumer, new StringTextComponent(I18n.format("minetogether.groupinvite.sure1")), new StringTextComponent(I18n.format("minetogether.groupinvite.sure2"))));
    }
    
    BooleanConsumer unmuteConsumer = new BooleanConsumer()
    {
        @Override
        public void accept(boolean t)
        {
            if (t)
            {
                MineTogether.instance.unmuteUser(unmutePlayer);
                refreshMutedList(false);
            }
            minecraft.displayGuiScreen(new FriendsListScreen(parent));
        }
    };
    
    public void unmutePlayer(String muted)
    {
        unmutePlayer = muted;
        minecraft.displayGuiScreen(new ConfirmScreen(unmuteConsumer, new StringTextComponent(I18n.format("minetogether.unmute.sure1")), new StringTextComponent(I18n.format("minetogether.unmute.sure2"))));
    }
}
