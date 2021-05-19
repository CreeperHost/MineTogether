package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

public class FriendRequestScreen extends Screen
{
    private final String playerName;
    private final String chatInternalName;
    private final String friendCode;
    private final boolean accept;
    private final String friendName;
    private final Screen parent;
    private Profile profile;
    private Button acceptBtn;
    private Button cancelBtn;
    boolean first = true;
    private EditBox nameEntry;
    Minecraft mc = Minecraft.getInstance();

    public FriendRequestScreen(Screen parent, String playerName, Profile friendTarget, String friendCode, String friendName, boolean accept)
    {
        super(new TranslatableComponent(""));
        this.playerName = playerName;
        this.chatInternalName = friendTarget == null ? "" : friendTarget.getCurrentIRCNick();
        this.friendCode = friendCode;
        this.accept = accept;
        this.parent = parent;
        this.friendName = friendName;
        this.profile = friendTarget;
    }
    
    @Override
    public void init()
    {
        super.init();
        buttons.clear();
        mc.keyboardHandler.setSendRepeatsToGui(true);
        
        this.addButton(cancelBtn = new Button(width / 2 - 180, height - 50, 80, 20, new TranslatableComponent("Cancel"), (button) ->
        {
            Minecraft.getInstance().setScreen(parent);
        }));
        
        this.addButton(acceptBtn = new Button(width / 2 + 100, height - 50, 80, 20, accept ? new TranslatableComponent("Accept") : new TranslatableComponent("Send request"), (buttons) ->
        {
            if (accept)
            {
                ChatHandler.acceptFriendRequest(chatInternalName, friendName);
                new Thread(() -> ChatCallbacks.addFriend(friendCode, nameEntry.getValue().trim(), MineTogetherClient.getUUID())).start();
            } else
            {
                ChatHandler.sendFriendRequest(chatInternalName, nameEntry.getValue().trim());
            }
            Minecraft.getInstance().setScreen(parent);
        }));
        
        nameEntry = new EditBox(mc.font, width / 2 - 100, height / 2 - 10, 200, 20, new TranslatableComponent(""));
        if (first)
            nameEntry.setValue(profile.getUserDisplay()); // default to player name
        first = false;
        
        acceptBtn.active = nameEntry.getValue().trim().length() >= 3;
        nameEntry.setFocus(true);
        nameEntry.setCanLoseFocus(false);
    }
    
    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
//        nameEntry.setEnableBackgroundDrawing(true);
        nameEntry.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, mc.font, accept ? I18n.get("minetogether.friend.acceptgui") : I18n.get("minetogether.friend.addgui"), width / 2, 5, 0xFFFFFFFF);
        drawCenteredString(matrixStack, mc.font, accept ? I18n.get("minetogether.friend.acceptname") : I18n.get("minetogether.friend.addname"), width / 2, height / 2 - 30, 0xFFFFFFFF);
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
        acceptBtn.active = nameEntry.getValue().trim().length() >= 3;
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
    
    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        nameEntry.charTyped(p_charTyped_1_, p_charTyped_2_);
        return false;
    }
}
