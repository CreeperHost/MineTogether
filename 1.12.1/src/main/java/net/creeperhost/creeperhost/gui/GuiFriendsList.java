package net.creeperhost.creeperhost.gui;

import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.gui.element.GuiTextFieldCompat;
import net.creeperhost.creeperhost.gui.list.GuiList;
import net.creeperhost.creeperhost.gui.list.GuiListEntryFriend;
import net.creeperhost.creeperhost.paul.Callbacks;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.Map;

public class GuiFriendsList extends GuiScreen
{
    private final GuiScreen parent;
    private GuiList list;
    private GuiButton buttonAdd;
    private GuiButton buttonCancel;
    private GuiButton buttonInvite;
    private GuiTextField codeEntry;
    private GuiTextField displayEntry;

    private boolean addFriend = false;
    private String friendCode;
    private boolean first = true;

    public GuiFriendsList(GuiScreen currentScreen)
    {
        this.parent = currentScreen;
        friendCode = Callbacks.getFriendCode();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        if (list == null)
            list = new GuiList(this, mc, width, height, 32, this.height - 64, 36);
        else
            list = new GuiList(this, mc, width, height, 32, this.height - 64, 36, list);

        if (first)
        {
            first = false;
            refreshFriendsList(true);
        }
        int y = this.height-60;
        buttonCancel = new GuiButton(0, this.width - 90, y, 80, 20, "gui.button.cancel");
        buttonList.add(buttonCancel);
        buttonAdd = new GuiButton(1, this.width / 2 - 40, y, 80, 20, "gui.button.addfriend");
        buttonList.add(buttonAdd);
        buttonInvite = new GuiButton(2, 10, y, 80, 20, "gui.button.invite");
        buttonInvite.enabled = false;
        buttonList.add(buttonInvite);
        codeEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 - 50, 160, 20);
        displayEntry = new GuiTextFieldCompat(3, this.fontRendererObj, this.width / 2 - 80, this.height / 2 + 0, 160, 20);
    }

    protected void refreshFriendsList(boolean force)
    {
        Map<String, Boolean> friends = Callbacks.getFriendsList(force);
        list.clearList();
        for(Map.Entry<String, Boolean> friend: friends.entrySet())
        {
            GuiListEntryFriend friendEntry = new GuiListEntryFriend(list, friend.getKey(), friend.getValue());
            list.addEntry(friendEntry);
        }
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
                list.addEntry(new GuiListEntryFriend(list, codeEntry.getText(), false));
                addFriend = false;
                buttonInvite.visible = true;
            }

        } else if (button.id == buttonInvite.id && button.enabled && button.visible) {
            refreshFriendsList(false); // here for now
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
            this.codeEntry.drawTextBox();
            this.displayEntry.drawTextBox();
        }

        this.drawCenteredString(this.fontRendererObj, Util.localize("gui.get_server"), this.width/2, 10, -1);
        this.drawString(this.fontRendererObj, Util.localize("gui.friendcode", friendCode), 10, this.height - 20, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
        this.codeEntry.mouseClicked(mouseX, mouseY, mouseButton);
        this.displayEntry.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.list.mouseClicked(mouseX, mouseY, mouseButton);
        if (list.getCurrSelected() != null)
            this.buttonInvite.enabled = true;
        else
            this.buttonInvite.enabled = false;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state){
        super.mouseReleased(mouseX, mouseY, state);
        this.list.mouseReleased(mouseX, mouseY, state);
    }
}
