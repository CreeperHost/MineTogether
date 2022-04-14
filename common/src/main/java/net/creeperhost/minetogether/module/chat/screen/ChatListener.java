package net.creeperhost.minetogether.module.chat.screen;

import io.sentry.Sentry;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.lib.chat.*;
import net.creeperhost.minetogether.lib.chat.data.Message;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.polylib.client.toast.SimpleToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ChatListener implements IChatListener
{
    public static ChatListener INSTANCE = new ChatListener();

    @Override
    public void onPartyInvite(Profile profile)
    {
        SimpleToast simpleToast = new SimpleToast(new TextComponent("New party invite from "), new TextComponent(profile.getFriendName()), Constants.MINETOGETHER_LOGO_LOCATION);
        Minecraft.getInstance().getToasts().addToast(simpleToast);

        //TODO add a runnable to SimpleToast
//        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("New party invite from " + profile.getFriendName()), 5000, ToastHandler.EnumToastType.DEFAULT, () ->
//        {
//            Screen currentScreen = Minecraft.getInstance().screen;
//            ConfirmScreen confirmScreen = new ConfirmScreen(accepted ->
//            {
//                if (accepted) ChatHandler.acceptPartyInvite(profile);
//
//                MineTogetherClient.toastHandler.clearToast(true);
//                Minecraft.getInstance().setScreen(currentScreen);
//            }, new TranslatableComponent("You have been invited to join a private party from " + profile.getFriendName()), new TranslatableComponent("Do you wish to accept the invite?"));
//
//            Minecraft.getInstance().setScreen(confirmScreen);
//        });
    }

    @Override
    public void onFriendOnline(Profile profile)
    {
        SimpleToast simpleToast = new SimpleToast(new TextComponent(profile.getFriendName() + " Is now online"), new TextComponent(" "), Constants.MINETOGETHER_LOGO_LOCATION);
        Minecraft.getInstance().getToasts().addToast(simpleToast);

//        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent(profile.getFriendName() + " Is now online"), 5000, ToastHandler.EnumToastType.DEFAULT, null);
    }

    @Override
    public void onFriendAccept(String name, String data)
    {
        SimpleToast simpleToast = new SimpleToast(new TextComponent(name + " Has accepted your friend request"), new TextComponent(" "), Constants.MINETOGETHER_LOGO_LOCATION);
        Minecraft.getInstance().getToasts().addToast(simpleToast);

//        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent(name + " Has accepted your friend request"), 5000, ToastHandler.EnumToastType.DEFAULT, null);

        ChatHandler.addMessageToChat(ChatHandler.CHANNEL, "FA:" + name, data);
        Profile profile = KnownUsers.findByNick(name);
        if (profile != null) {
            CompletableFuture.runAsync(() ->
            {
                try
                {
                    ChatCallbacks.addFriend(profile.getFriendCode(), data, MineTogetherClient.getPlayerHash());
                } catch (IOException e)
                {
                    Sentry.captureException(e);
                }
            }, MineTogetherChat.otherExecutor);
        }
    }

    @Override
    public void onFriendRequest(String user, String data)
    {
        SimpleToast simpleToast = new SimpleToast(new TextComponent(user + " Has sent you a friend request"), new TextComponent(" "), Constants.MINETOGETHER_LOGO_LOCATION);
        Minecraft.getInstance().getToasts().addToast(simpleToast);

//        MineTogetherClient.toastHandler.displayToast(new TranslatableComponent(user + " Has sent you a friend request"), 5000, ToastHandler.EnumToastType.DEFAULT, null);
        ChatHandler.addMessageToChat(ChatHandler.CHANNEL, "FR:" + user, data);
    }

    @Override
    public String onServerIdRequest()
    {
        return MineTogetherClient.getServerIDAndVerify();
    }

    @Override
    public void sendMessage(String channel, Message message)
    {
        ChatModule.sendMessage(channel, ChatFormatter.formatLine(message));
    }

    @Override
    public void setHasNewMessage(boolean value)
    {
        ChatModule.hasNewMessage = value;
    }

    @Override
    public String getVerifyOutput()
    {
        return MineTogetherChat.INSTANCE.signature + ":" + MineTogetherClient.getUUID() + ":" + MineTogetherClient.getServerIDAndVerify();
    }
}
