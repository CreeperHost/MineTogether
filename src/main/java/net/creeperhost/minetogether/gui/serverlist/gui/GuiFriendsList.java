package net.creeperhost.minetogether.gui.serverlist.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.data.Profile;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.GuiYahNah;
import net.creeperhost.minetogether.gui.element.ButtonString;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntryFriend;
import net.creeperhost.minetogether.gui.list.GuiListEntryMuted;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.data.FriendStatusResponse;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;

public class GuiFriendsList extends GuiScreen implements GuiYesNoCallback
{
    private final GuiScreen parent;
    private GuiList<GuiListEntryFriend> list;
    private GuiList<GuiListEntryMuted> listMuted;
    
    private GuiButton buttonAdd;
    private GuiButton buttonCancel;
    private GuiButton buttonInvite;
    private GuiButton buttonCopy;
    private GuiButton buttonRefresh;
    private GuiButton buttonChat;
    private GuiButton buttonRemove;
    private GuiButton toggle;
    private GuiButton channelInviteButton;
    private GuiTextFieldCompat codeEntry;
    private GuiTextFieldCompat displayEntry;
    private GuiTextFieldCompat searchEntry;

    private boolean addFriend = false;
    private String friendCode;
    private boolean first = true;
    private String friendDisplayString;
    private String errorText = null;
    private String hoveringText = null;
    private String lastHoveringText = null;
    private ArrayList<String> hoverTextCache = null;
    private Friend removeFriend;
    private String unmutePlayer;
    private Friend invitedPlayer;
    private boolean channelInvite = false;
    private boolean isMuted = false;
    
    public GuiFriendsList(GuiScreen currentScreen)
    {
        this.parent = currentScreen;
        friendCode = Callbacks.getFriendCode();
        CreeperHost.instance.clearToast(false);
    }
    
    @Override
    public void initGui()
    {
        if (!CreeperHost.instance.gdpr.hasAcceptedGDPR())
        {
            mc.displayGuiScreen(new GuiGDPR(parent, () -> new GuiFriendsList(parent)));
            return;
        }
        super.initGui();

        if(listMuted == null)
        {
            listMuted = new GuiList(this, mc, width, height, 32, this.height - 64, 36);
        } else {
            listMuted.setDimensions(width, height, 32, this.height - 64);
        }
        
        if (list == null)
        {
            list = new GuiList(this, mc, width, height, 32, this.height - 64, 36);
        }
        else
        {
            list.setDimensions(width, height, 32, this.height - 64);
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
        
        buttonCancel = new GuiButton(0, buttonX, y, buttonWidth, 20, Util.localize("button.cancel"));
        buttonList.add(buttonCancel);
        buttonX += spaceInbetween;

        buttonAdd = new GuiButton(1, buttonX, y, buttonWidth, 20, Util.localize("multiplayer.button.addfriend"));
        buttonList.add(buttonAdd);
        buttonX += spaceInbetween;

        buttonInvite = new GuiButton(4, buttonX, y, buttonWidth, 20, Util.localize("multiplayer.button.invite"));
        buttonInvite.enabled = list.getCurrSelected() != null;
        buttonList.add(buttonInvite);
        
        codeEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 - 50, 160, 20);
        displayEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 + 0, 160, 20);
        
        friendDisplayString = CreeperHost.profile.get().getFriendCode();
        buttonCopy = new ButtonString(4, 10 + 5, this.height - 26, friendDisplayString);
        buttonList.add(buttonCopy);

        buttonRefresh = new GuiButton(1337, this.width - 90, this.height - 26, 80, 20, Util.localize("multiplayer.button.refresh"));
        buttonList.add(buttonRefresh);
        
        toggle = new GuiButton(5, width - 60,   6, 60, 20,isMuted ? "Friends" : "Muted");
        buttonList.add(toggle);

        searchEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, y + 28, 160, 20);
        searchEntry.setVisible(true);
    }
    
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
                        list.addEntry(friendEntry);
                    }
                }
                else
                    {
                        list.addEntry(friendEntry);
                    }
            }
        }
    }


    protected void refreshMutedList(boolean force)
    {
        ArrayList<String> mutedUsers = CreeperHost.mutedUsers;
        listMuted.clearList();
        if (mutedUsers != null)
        {
            for (String mute : mutedUsers)
            {
                String username = CreeperHost.instance.getNameForUser(mute);
                GuiListEntryMuted mutedEntry = new GuiListEntryMuted(this, listMuted, username);
                if(searchEntry != null && !searchEntry.getText().isEmpty())
                {
                    String s = searchEntry.getText();
                    if(mute.toLowerCase().contains(s.toLowerCase()))
                    {
                        listMuted.addEntry(mutedEntry);
                    }
                }
                else
                    {
                        listMuted.addEntry(mutedEntry);
                    }
            }
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void onGuiClosed()
    {
        CreeperHost.instance.clearToast(false);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button == buttonCancel)
        {
            if (!addFriend)
                mc.displayGuiScreen(parent);
            else
            {
                addFriend = false;
                buttonInvite.visible = true;
                codeEntry.setText("");
            }
        } else if (button == buttonAdd)
        {
            if (!addFriend)
            {
                addFriend = true;
                buttonInvite.visible = false;
            } else if (!codeEntry.getText().isEmpty())
            {
                FriendStatusResponse result = Callbacks.addFriend(codeEntry.getText(), displayEntry.getText());
                addFriend = false;
                if (result == null) {
                    Profile profile = new Profile(result.getHash());
                    if(profile != null) {
                        list.addEntry(new GuiListEntryFriend(this, list, new Friend(profile, displayEntry.getText(), codeEntry.getText(), false)));
                    }
                }
                buttonInvite.visible = true;
                showAlert(result.getMessage().isEmpty() ? Util.localize("multiplayer.friendsent") : result.getMessage(), 0x00FF00, 5000);
            }
            
        } else if (button == buttonInvite && button.enabled && button.visible)
        {
            if (CreeperHost.instance.curServerId == -1)
            {
                showAlert(Util.localize("multiplayer.notinvite"), 0xFF0000, 5000);
                return;
            } else
            {
                boolean ret = Callbacks.inviteFriend(list.getCurrSelected().getFriend());
                if (ret)
                {
                    Callbacks.inviteFriend(list.getCurrSelected().getFriend());
                    showAlert(Util.localize("multiplayer.invitesent"), 0x00FF00, 5000);
                } else
                {
                    showAlert(Util.localize("multiplayer.couldnotinvite"), 0xFF0000, 5000);
                }
            }
        } else if (button == buttonCopy)
        {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(CreeperHost.profile.get().getFriendCode()), null);
            showAlert("Copied to clipboard.", 0x00FF00, 5000);
        } else if (button == buttonRefresh)
        {
            refreshFriendsList(false);
            refreshMutedList(false);
        }
        else if(button.id == toggle.id)
        {
            if(button.displayString.contains("Muted"))
            {
                button.displayString = "Friends";
                isMuted = true;
            }
            else if(button.displayString.contains("Friends"))
            {
                button.displayString = "Muted";
                isMuted = false;
            }
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground(0);
        if(!isMuted)
        {
            if (!addFriend)
            {
                this.list.drawScreen(mouseX, mouseY, partialTicks);
                this.searchEntry.setVisible(false);
            }
            else
            {
                this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.othercode"), this.width / 2, this.height / 2 - 60, 0xFFFFFF);
                this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.displayname"), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
                this.codeEntry.drawTextBox();
                this.displayEntry.drawTextBox();
            }
            this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.friends"), this.width / 2, 10, -1);
        }
        else
        {
            this.listMuted.drawScreen(mouseX, mouseY, partialTicks);
            this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.muted"), this.width / 2, 10, -1);
            this.searchEntry.setVisible(false);
        }

        this.drawString(this.fontRendererObj, I18n.format("creeperhost.multiplayer.friendcode"), 10, this.height - 20, -1);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        if (hoveringText != null)
        {
            if (hoveringText != lastHoveringText)
            {
                hoverTextCache = new ArrayList<>();
                hoverTextCache.add(hoveringText);
                lastHoveringText = hoveringText;
            }
            drawHoveringText(hoverTextCache, mouseX + 12, mouseY);
        }
        if(searchEntry != null) this.searchEntry.drawTextBox();
    }
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (codeEntry.isFocused())
        {
            codeEntry.textboxKeyTyped(typedChar, keyCode);
        }
        else if (displayEntry.isFocused())
        {
            displayEntry.textboxKeyTyped(typedChar, keyCode);
        }
        else if (searchEntry.isFocused())
        {
            searchEntry.textboxKeyTyped(typedChar, keyCode);
            refreshFriendsList(false);
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        this.codeEntry.myMouseClicked(mouseX, mouseY, mouseButton);
        this.displayEntry.myMouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(!isMuted)
        {
            this.list.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            this.listMuted.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (list.getCurrSelected() != null)
            if (list.getCurrSelected().getFriend().isAccepted())
                this.buttonInvite.enabled = true;
            else
                this.buttonInvite.enabled = false;
        else
            this.buttonInvite.enabled = false;

        this.searchEntry.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        if(isMuted) {
            this.list.mouseReleased(mouseX, mouseY, state);
        }
        else {
            this.listMuted.mouseReleased(mouseX, mouseY, state);
        }
    }
    
    private void showAlert(String text, int colour, int time)
    {
        CreeperHost.instance.displayToast(text, time, null);
    }
    
    public void setHoveringText(String hoveringText)
    {
        this.hoveringText = hoveringText;
    }
    
    public void removeFriend(Friend friend)
    {
        removeFriend = friend;
        mc.displayGuiScreen(new GuiYesNo(this, I18n.format("minetogether.removefriend.sure1"), I18n.format("minetogether.removefriend.sure2"), 0));
    }

    public void inviteGroupChat(Friend invited)
    {
        invitedPlayer = invited;
        mc.displayGuiScreen(new GuiYahNah(this, I18n.format("minetogether.groupinvite.sure1"), I18n.format("minetogether.groupinvite.sure2"), 1));
    }

    public void unmutePlayer(String muted)
    {
        unmutePlayer = muted;
        mc.displayGuiScreen(new GuiYesNo(this, I18n.format("minetogether.unmute.sure1"), I18n.format("minetogether.unmute.sure2"), 2));
    }
    
    @Override
    public void confirmClicked(boolean result, int id)
    {
        if (result)
        {
            if(id == 0)
            {
                Callbacks.removeFriend(removeFriend.getCode());
                refreshFriendsList(true);
            }
            else if(id == 2)
            {
                CreeperHost.instance.unmuteUser(unmutePlayer);
                listMuted.clearList();
                refreshMutedList(false);
            }
            else if(id == 1)
            {
                if (!invitedPlayer.isAccepted())
                    showAlert("Cannot invite pending friends", 0x00FF00, 5000);
                else {
                    String friendCode = "MT" + invitedPlayer.getCode().substring(0, 28);
                    showAlert("Sent invite to " + invitedPlayer.getName(), 0x00FF00, 5000);
                    ChatHandler.sendChannelInvite(friendCode, CreeperHost.instance.ourNick);
                }
            }
        }
        mc.displayGuiScreen(this);
    }
}
