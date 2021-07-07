package net.creeperhost.minetogether.module.chat.screen;

import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.IChatListener;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import java.net.MalformedURLException;
import java.net.URL;

public class ChatListener implements IChatListener
{
    public static ChatListener INSTANCE = new ChatListener();

    @Override
    public void onPartyInvite(Profile profile)
    {
        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("New party invite from " + profile.getFriendName()), 5000, ToastHandler.EnumToastType.DEFAULT, () ->
        {
            Screen currentScreen = Minecraft.getInstance().screen;
            ConfirmScreen confirmScreen = new ConfirmScreen(accepted ->
            {
                if(accepted) ChatHandler.acceptPartyInvite(profile);

                MineTogetherClient.toastHandler.clearToast(true);
                Minecraft.getInstance().setScreen(currentScreen);
            }, new TranslatableComponent("You have been invited to join a private party from " + profile.getFriendName()), new TranslatableComponent("Do you wish to accept the invite?"));

            Minecraft.getInstance().setScreen(confirmScreen);
        });
    }

    @Override
    public void onFriendOnline(Profile profile)
    {
        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent(profile.getFriendName() + " Is now online"), 5000, ToastHandler.EnumToastType.DEFAULT, null);
    }

    @Override
    public String onServerIdRequest()
    {
        return MineTogetherClient.getServerIDAndVerify();
    }

    @Override
    public void sendMessage(Message message)
    {
        ChatModule.sendMessage(ChatFormatter.formatLine(message));
    }

    @Override
    public void setHasNewMessage(boolean value)
    {
        ChatModule.hasNewMessage = value;
    }

    @Override
    public void onFriendAccept(String name)
    {
        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent(name + " Has accepted your friend request"), 5000, ToastHandler.EnumToastType.DEFAULT, null);
    }
}
