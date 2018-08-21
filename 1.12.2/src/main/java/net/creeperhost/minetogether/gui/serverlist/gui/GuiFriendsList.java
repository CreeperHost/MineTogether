package net.creeperhost.minetogether.gui.serverlist.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntryFriend;
import net.creeperhost.minetogether.serverlist.data.Friend;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;

public class GuiFriendsList extends GuiScreen implements GuiYesNoCallback
{
    private final GuiScreen parent;
    private GuiList<GuiListEntryFriend> list;
    private GuiButton buttonAdd;
    private GuiButton buttonCancel;
    private GuiButton buttonInvite;
    private GuiButton buttonCopy;
    private GuiButton buttonRefresh;
    private GuiButton buttonChat;
    private GuiButton buttonRemove;
    private GuiTextFieldCompat codeEntry;
    private GuiTextFieldCompat displayEntry;

    private boolean addFriend = false;
    private String friendCode;
    private boolean first = true;
    private String friendDisplayString;
    private String errorText = null;
    private String hoveringText = null;
    private String lastHoveringText = null;
    private ArrayList<String> hoverTextCache = null;
    private Friend removeFriend;

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
        if (list == null)
            list = new GuiList(this, mc, width, height, 32, this.height - 64, 36);
        else
            list.setDimensions(width, height, 32, this.height - 64);

        if (first)
        {
            first = false;
            refreshFriendsList(true);
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
        /*buttonRemove = new GuiButton(2, buttonX, y, buttonWidth, 20, "Remove friend");
        buttonList.add(buttonRemove);
        buttonX += spaceInbetween;*/
        /*buttonChat = new GuiButton(3, buttonX, y, buttonWidth, 20, "Open chat");
        buttonList.add(buttonChat);
        buttonX += spaceInbetween;*/
        buttonInvite = new GuiButton(4, buttonX, y, buttonWidth, 20, Util.localize("multiplayer.button.invite"));
        buttonInvite.enabled = list.getCurrSelected() != null;
        buttonList.add(buttonInvite);

        codeEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 - 50, 160, 20);
        displayEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 + 0, 160, 20);

        friendDisplayString = Util.localize("multiplayer.friendcode", friendCode);
        int friendWidth = fontRendererObj.getStringWidth(friendDisplayString);
        buttonCopy = new GuiButton(4, 10 + friendWidth + 3, this.height - 26, 80, 20, Util.localize("multiplayer.button.copy"));
        buttonList.add(buttonCopy);
        buttonRefresh = new GuiButton(1337, this.width - 90, this.height - 26, 80, 20, Util.localize("multiplayer.button.refresh"));
        buttonList.add(buttonRefresh);
    }

    protected void refreshFriendsList(boolean force)
    {
        ArrayList<Friend> friends = Callbacks.getFriendsList(force);
        list.clearList();
        if(friends != null)
        {
            for (Friend friend : friends)
            {
                GuiListEntryFriend friendEntry = new GuiListEntryFriend(this, list, friend);
                list.addEntry(friendEntry);
            }
        }
    }

    @Override
    public void onGuiClosed()
    {
        CreeperHost.instance.clearToast(false);
    }

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
        }
        else if (button == buttonAdd)
        {
            if (!addFriend)
            {
                addFriend = true;
                buttonInvite.visible = false;
            }
            else if (!codeEntry.getText().isEmpty())
            {
                String result = Callbacks.addFriend(codeEntry.getText(), displayEntry.getText());
                addFriend = false;
                if (result == null)
                    list.addEntry(new GuiListEntryFriend(this, list, new Friend(displayEntry.getText(), codeEntry.getText(), false)));
                buttonInvite.visible = true;
                showAlert(result == null ? Util.localize("multiplayer.friendsent") : result, 0x00FF00, 5000);
            }

        }
        else if (button == buttonInvite && button.enabled && button.visible)
        {
            if (CreeperHost.instance.curServerId == -1)
            {
                showAlert(Util.localize("multiplayer.notinvite"), 0xFF0000, 5000);
                return;
            }
            else
            {
                boolean ret = Callbacks.inviteFriend(list.getCurrSelected().getFriend());
                if (ret)
                {
                    Callbacks.inviteFriend(list.getCurrSelected().getFriend());
                    showAlert(Util.localize("multiplayer.invitesent"), 0x00FF00, 5000);
                }
                else
                {
                    showAlert(Util.localize("multiplayer.couldnotinvite"), 0xFF0000, 5000);
                }

            }
        }
        else if (button == buttonCopy)
        {
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                    new StringSelection(friendCode),
                    null
                );
            showAlert("Copied to clipboard.", 0x00FF00, 5000);
        }
        else if (button == buttonRefresh)
        {
            refreshFriendsList(false);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground(0);

        if (!addFriend)
            this.list.drawScreen(mouseX, mouseY, partialTicks);
        else
        {
            this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.othercode"), this.width / 2, this.height / 2 - 60, 0xFFFFFF);
            this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.displayname"), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
            this.codeEntry.drawTextBox();
            this.displayEntry.drawTextBox();
        }

        this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.friends"), this.width / 2, 10, -1);
        this.drawString(this.fontRendererObj, friendDisplayString, 10, this.height - 20, -1);

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
            codeEntry.textboxKeyTyped(typedChar, keyCode);
        else if (displayEntry.isFocused())
            displayEntry.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        this.codeEntry.myMouseClicked(mouseX, mouseY, mouseButton);
        this.displayEntry.myMouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.list.mouseClicked(mouseX, mouseY, mouseButton);
        if (list.getCurrSelected() != null)
            if (list.getCurrSelected().getFriend().isAccepted())
                this.buttonInvite.enabled = true;
            else
                this.buttonInvite.enabled = false;
        else
            this.buttonInvite.enabled = false;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.list.mouseReleased(mouseX, mouseY, state);
    }

    private void showAlert(String text, int colour, int time)
    {
        CreeperHost.instance.displayToast(text, time);
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

    @Override
    public void confirmClicked(boolean result, int id) {
        if (result)
        {
            Callbacks.removeFriend(removeFriend.getCode());
            refreshFriendsList(true);
        }
        mc.displayGuiScreen(this);
    }
}
