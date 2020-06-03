package net.creeperhost.minetogether.client.screen.chat;

import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

public class ChatFriendScreen extends Screen
{
    private final String playerName;
    private final String chatInternalName;
    private final String friendCode;
    private final boolean accept;
    private final String friendName;
    private final Screen parent;
    private Button acceptBtn;
    private Button cancelBtn;
    boolean first = true;
    private TextFieldWidget nameEntry;
    Minecraft mc = Minecraft.getInstance();
    
    public ChatFriendScreen(Screen parent, String playerName, String chatInternalName, String friendCode, String friendName, boolean accept)
    {
        super(new StringTextComponent(""));
        this.playerName = playerName;
        this.chatInternalName = chatInternalName;
        this.friendCode = friendCode;
        this.accept = accept;
        this.parent = parent;
        this.friendName = friendName;
    }
    
    @Override
    public void init()
    {
        super.init();
        buttons.clear();
        mc.keyboardListener.enableRepeatEvents(true);
        
        this.addButton(cancelBtn = new Button(width / 2 - 180, height - 50, 80, 20, "Cancel", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen(parent);
        }));
        
        this.addButton(acceptBtn = new Button(width / 2 + 100, height - 50, 80, 20, accept ? "Accept" : "Send request", (buttons) ->
        {
            if (accept)
            {
                ChatHandler.acceptFriendRequest(chatInternalName, nameEntry.getText().trim());
                new Thread(() -> Callbacks.addFriend(friendCode, friendName)).start();
            } else
            {
                ChatHandler.sendFriendRequest(chatInternalName, nameEntry.getText().trim());
            }
            Minecraft.getInstance().displayGuiScreen(parent);
        }));
        
        nameEntry = new TextFieldWidget(mc.fontRenderer, width / 2 - 100, height / 2 - 10, 200, 20, "");
        if (first)
            nameEntry.setText(playerName); // default to player name
        first = false;
        
        acceptBtn.active = nameEntry.getText().trim().length() >= 3;
        nameEntry.setFocused2(true);
        nameEntry.setCanLoseFocus(false);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        super.render(mouseX, mouseY, partialTicks);
        nameEntry.setEnableBackgroundDrawing(true);
        nameEntry.render(mouseX, mouseY, partialTicks);
        drawCenteredString(mc.fontRenderer, accept ? I18n.format("minetogether.friend.acceptgui") : I18n.format("minetogether.friend.addgui"), width / 2, 5, 0xFFFFFFFF);
        drawCenteredString(mc.fontRenderer, accept ? I18n.format("minetogether.friend.acceptname") : I18n.format("minetogether.friend.addname"), width / 2, height / 2 - 30, 0xFFFFFFFF);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        nameEntry.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        nameEntry.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        acceptBtn.active = nameEntry.getText().trim().length() >= 3;
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
    
    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        nameEntry.charTyped(p_charTyped_1_, p_charTyped_2_);
        return false;
    }
}
