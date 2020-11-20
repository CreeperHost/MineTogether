package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.data.Profile;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.misc.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class GuiChatFriend extends GuiScreen
{
    private final String playerName;
    private final String chatInternalName;
    private final String friendCode;
    private final boolean accept;
    private final String friendName;
    private final GuiScreen parent;
    private GuiButton acceptBtn;
    private GuiButton cancelBtn;
    private GuiTextField nameEntry;
    private Profile friendProfile;
    
    public GuiChatFriend(GuiScreen parent, String playerName, Profile friendTarget, String friendCode, String friendName, boolean accept)
    {
        this.playerName = playerName;
        this.chatInternalName = friendTarget == null ? "" : friendTarget.getCurrentIRCNick();
        this.friendCode = friendCode;
        this.accept = accept;
        this.parent = parent;
        this.friendName = friendName;
        this.friendProfile = friendTarget;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        nameEntry.drawTextBox();
        drawCenteredString(fontRendererObj, accept ? I18n.format("minetogether.friend.acceptgui") : I18n.format("minetogether.friend.addgui"), width / 2, 5, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, accept ? I18n.format("minetogether.friend.acceptname") : I18n.format("minetogether.friend.addname"), width / 2, height / 2 - 30, 0xFFFFFFFF);
    }
    
    boolean first = true;
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        buttonList.add(cancelBtn = new GuiButton(0, width / 2 - 180, height - 50, 80, 20, "Cancel"));
        buttonList.add(acceptBtn = new GuiButton(1, width / 2 + 100, height - 50, 80, 20, accept ? "Accept" : "Send request"));
        
        nameEntry = new GuiTextFieldCompat(0, fontRendererObj, width / 2 - 100, height / 2 - 10, 200, 20);
        if (first)
        {
            if(friendProfile != null && !friendProfile.getUserDisplay().isEmpty()) nameEntry.setText(friendProfile.getUserDisplay()); // default to player name
        }
        first = false;
        
        acceptBtn.enabled = nameEntry.getText().trim().length() >= 3;
        nameEntry.setFocused(true);
        nameEntry.setCanLoseFocus(false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        nameEntry.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button == cancelBtn)
        {
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
        else if (button == acceptBtn)
        {
            if (accept)
            {
                ChatHandler.acceptFriendRequest(chatInternalName, friendName);
                new Thread(() -> Callbacks.addFriend(friendCode, nameEntry.getText().trim())).start();
            }
            else
            {
                ChatHandler.sendFriendRequest(chatInternalName, nameEntry.getText().trim());
            }
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        nameEntry.textboxKeyTyped(typedChar, keyCode);
        acceptBtn.enabled = nameEntry.getText().trim().length() >= 3;
        super.keyTyped(typedChar, keyCode);
    }
}
