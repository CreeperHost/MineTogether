package net.creeperhost.minetogether.mixin;

import com.google.common.collect.Lists;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.GuiButtonPair;
import net.creeperhost.minetogether.module.connect.FriendsServerList;
import net.creeperhost.minetogether.util.MathHelper;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatScreen.class)
public class MixinChatScreen extends Screen
{
    private GuiButtonPair switchButton;

    protected MixinChatScreen(Component component)
    {
        super(component);
    }

    @Inject(at=@At("TAIL"), method="init()V")
    public void init(CallbackInfo ci)
    {
        int x = MathHelper.ceil(((float) Minecraft.getInstance().gui.getChat().getWidth())) + 16 + 2;

        addButton(switchButton = new GuiButtonPair(x, height - 215, 234, 16, 0, false, false, true, p ->
        {
            ChatModule.showMTChat = switchButton.activeButton == 1;

        }, I18n.get("minetogether.ingame.chat.local"), I18n.get("minetogether.ingame.chat.global")));
    }

    @Override
    public void sendMessage(String string)
    {
        if(ChatModule.showMTChat)
        {
            ChatHandler.sendMessage(ChatHandler.CHANNEL, string);
            Minecraft.getInstance().gui.getChat().addRecentChat(string);
            return;
        }
        super.sendMessage(string);
    }
}
