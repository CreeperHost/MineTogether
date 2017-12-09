package net.creeperhost.creeperhost.gui.serverlist;

import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.gui.element.GuiTextFieldCompat;
import net.creeperhost.creeperhost.gui.list.GuiList;
import net.creeperhost.creeperhost.gui.list.GuiListEntryFriend;
import net.creeperhost.creeperhost.gui.serverlist.Friend;
import net.creeperhost.creeperhost.paul.Callbacks;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;

import javax.security.auth.callback.Callback;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class GuiFriendsList extends GuiScreen
{
    private final GuiScreen parent;
    private GuiList<GuiListEntryFriend> list;
    private GuiButton buttonAdd;
    private GuiButton buttonCancel;
    private GuiButton buttonInvite;
    private GuiButton buttonCopy;
    private GuiButton buttonRefresh;
    private GuiTextFieldCompat codeEntry;
    private GuiTextFieldCompat displayEntry;

    private boolean addFriend = false;
    private String friendCode;
    private boolean first = true;
    private String friendDisplayString;

    public GuiFriendsList(GuiScreen currentScreen)
    {
        this.parent = currentScreen;
        friendCode = Callbacks.getFriendCode();
        CreeperHost.instance.clearToast(false);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        if (list == null)
            list = new GuiList(this, mc, width, height, 32, this.height - 64, 36);
        else
            list.setDimensions(width, height, 32, this.height-64);

        if (first)
        {
            first = false;
            refreshFriendsList(true);
        }
        int y = this.height-60;
        buttonCancel = new GuiButton(0, this.width - 90, y, 80, 20, Util.localize("button.cancel"));
        buttonList.add(buttonCancel);
        buttonRefresh = new GuiButton(1337, this.width - 90, y + 30, 80, 20, Util.localize("multiplayer.button.refresh"));
        buttonList.add(buttonRefresh);
        buttonAdd = new GuiButton(1, this.width / 2 - 40, y, 80, 20, Util.localize("multiplayer.button.addfriend"));
        buttonList.add(buttonAdd);
        buttonInvite = new GuiButton(2, 10, y, 80, 20, Util.localize("multiplayer.button.invite"));
        buttonInvite.enabled = list.getCurrSelected() != null;
        buttonList.add(buttonInvite);
        friendDisplayString = Util.localize("multiplayer.friendcode", friendCode);
        int friendWidth = fontRendererObj.getStringWidth(friendDisplayString);
        buttonCopy = new GuiButton(4, 10 + friendWidth + 3, this.height - 26, 80, 20, Util.localize("multiplayer.button.copy"));
        buttonList.add(buttonCopy);
        codeEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 - 50, 160, 20);
        displayEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 + 0, 160, 20);
    }

    protected void refreshFriendsList(boolean force)
    {
        ArrayList<Friend> friends = Callbacks.getFriendsList(force);
        list.clearList();
        for(Friend friend: friends)
        {
            GuiListEntryFriend friendEntry = new GuiListEntryFriend(list, friend);
            list.addEntry(friendEntry);
        }
    }

    @Override
    public void onGuiClosed()
    {
        CreeperHost.instance.clearToast(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException{
        if (button.id == buttonCancel.id)
        {
            if (!addFriend)
                mc.displayGuiScreen(parent);
            else
            {
                addFriend = false;
                buttonInvite.visible = true;
                codeEntry.setText("");
            }
        } else if (button.id == buttonAdd.id) {
            if (!addFriend)
            {
                addFriend = true;
                buttonInvite.visible = false;
            } else if (!codeEntry.getText().isEmpty()){
                Callbacks.addFriend(codeEntry.getText(), displayEntry.getText());
                addFriend = false;
                list.addEntry(new GuiListEntryFriend(list, new Friend(displayEntry.getText(), codeEntry.getText(), false)));
                buttonInvite.visible = true;
                showAlert(Util.localize("multiplayer.friendsent"), 0x00FF00, 5000);
            }

        } else if (button.id == buttonInvite.id && button.enabled && button.visible) {
            if (CreeperHost.instance.curServerId == -1)
            {
                showAlert(Util.localize("multiplayer.notinvite"), 0xFF0000, 5000);
                return;
            } else {
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
        } else if (button.id == buttonCopy.id) {
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                  new StringSelection(friendCode),
                  null
                );
            showAlert("Copied to clipboard.", 0x00FF00, 5000);
        } else if (button.id == buttonRefresh.id) {
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

        this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.friends"), this.width/2, 10, -1);
        this.drawString(this.fontRendererObj, friendDisplayString, 10, this.height - 20, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
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
    protected void mouseReleased(int mouseX, int mouseY, int state){
        super.mouseReleased(mouseX, mouseY, state);
        this.list.mouseReleased(mouseX, mouseY, state);
    }

    private void showAlert(String text, int colour, int time)
    {
        CreeperHost.instance.displayToast(text, time);
    }
}
