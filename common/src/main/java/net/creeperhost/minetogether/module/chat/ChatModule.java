package net.creeperhost.minetogether.module.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.hooks.client.screen.ScreenHooks;
import dev.architectury.platform.Platform;
import io.sentry.Sentry;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.MineTogetherCommon;
import net.creeperhost.minetogether.MinetogetherExpectPlatform;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.MineTogether;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;
import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.minetogether.lib.chat.KnownUsers;
import net.creeperhost.minetogether.lib.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.mixin.ChatComponentInvoker;
import net.creeperhost.minetogether.module.chat.screen.ChatListener;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogether.threads.FriendUpdateThread;
import net.creeperhost.minetogether.verification.ModPackVerifier;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.creeperhost.polylib.client.screen.widget.buttons.ButtonMultiple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ChatModule
{
    public static ClientChatTarget clientChatTarget = ClientChatTarget.DEFAULT;
    public static ClientChatTarget lastSelected = ClientChatTarget.DEFAULT;
    public static boolean hasNewMessage = true;
    public static ArrayList<String> mutedUsers = new ArrayList<>();
    public static Path mutedUsersPath = Platform.getGameFolder().resolve("local/minetogether/mutedusers.json");
    private static MineTogetherChat mineTogetherChat;

    public static void init()
    {
        MineTogether.init("MineTogether-mod/" + Constants.VERSION + " Launcher/" + Minecraft.getInstance().getLaunchedVersion() + " Minecraft/" + Platform.getMinecraftVersion() + " Modloader/" + MinetogetherExpectPlatform.getModLoader());
        String ourNick = "MT" + ChatCallbacks.getPlayerHash(MineTogetherClient.getUUID()).substring(0, 28);
        FriendUpdateThread.init();
        buildChat(ourNick);
        loadMutedList();
    }

    public static void buildChat(String ourNick)
    {
        MineTogetherCommon.logger.info("Building MineTogether chat");
        boolean online = MineTogetherClient.isOnlineUUID;
        String realName = new ModPackVerifier().verify();
        String signature = new SignatureVerifier().verify();
        String serverID = MineTogetherClient.getServerIDAndVerify();

        mineTogetherChat = new MineTogetherChat(ourNick, MineTogetherClient.getPlayerHash(), online, realName, signature, serverID, ChatListener.INSTANCE);
        Sentry.setTag("isOnline", String.valueOf(online));
        Sentry.setTag("hash", MineTogetherClient.getPlayerHash());
        Sentry.setTag("signature", signature);

        if(serverID != null && !serverID.isEmpty())
        {
            mineTogetherChat.startChat();
        }
    }

    public static void onScreenOpen(Screen screen, ScreenAccess screenAccess)
    {
        Button friendsButton = new Button(screen.width - 105, 5, 100, 20, Component.translatable("minetogether.multiplayer.friends"), p -> Minecraft.getInstance().setScreen(new FriendsListScreen(screen)));

        Button chatButton = new ButtonMultiple(screen.width - 125, 5, Config.getInstance().isChatEnabled() ? 1 : 3, Constants.WIDGETS_LOCATION, p -> Minecraft.getInstance().setScreen(Config.getInstance().isChatEnabled() ? new ChatScreen(screen) : new SettingsScreen(screen)));

        if (screen instanceof TitleScreen && Config.getInstance().isEnableMainMenuFriends())
        {
            if (Config.instance.isMainMenuEnabled())
            {
                ScreenHooks.addRenderableWidget(screen, friendsButton);
                ScreenHooks.addRenderableWidget(screen, chatButton);
                friendsButton.active = !Config.getInstance().getFirstConnect();
            }
        }
        if (screen instanceof PauseScreen)
        {
            ScreenHooks.addRenderableWidget(screen, friendsButton);
            ScreenHooks.addRenderableWidget(screen, chatButton);
            friendsButton.active = !Config.getInstance().getFirstConnect();
        }
    }

    public static void sendMessage(String channel, Component component)
    {
        if (ChatModule.clientChatTarget != ClientChatTarget.DEFAULT)
        {
//            Component newComp = Component.literal("[!MineTogetherMessage]" + component.getString()).withStyle(component.getStyle());
            ClientChatTarget current = ChatModule.clientChatTarget;
            if (channel.equals(ChatHandler.CHANNEL))
            {
                ChatModule.clientChatTarget = ClientChatTarget.MINETOGETHER;
//                Minecraft.getInstance().gui.getChat().addMessage(newComp);
                ((ChatComponentInvoker) Minecraft.getInstance().gui.getChat()).invokeAddMessage(component, 0, Minecraft.getInstance().gui.getGuiTicks(), false);
            }
            if (ChatHandler.hasParty && channel.equals(ChatHandler.currentParty))
            {
                ChatModule.clientChatTarget = ClientChatTarget.PARTY;
//                Minecraft.getInstance().gui.getChat().addMessage(newComp);
                ((ChatComponentInvoker) Minecraft.getInstance().gui.getChat()).invokeAddMessage(component, 0, Minecraft.getInstance().gui.getGuiTicks(), false);
            }
            //Reset
            ChatModule.clientChatTarget = current;
        }
    }

    public static void muteUser(String user)
    {
        //Don't add the user if they are already in the list
        if (mutedUsers.contains(user)) return;

        mutedUsers.add(user);
        CompletableFuture.runAsync(() ->
        {
            Profile profile = KnownUsers.findByHash(user);
            if (profile == null) profile = KnownUsers.add(user);
            try
            {
                profile.loadProfile();
            } catch (IOException e)
            {
                Sentry.captureException(e);
            }
            profile.setMuted(true);
            KnownUsers.update(profile);
        }, MineTogetherChat.profileExecutor);

        Gson gson = new Gson();
        try
        {
            if (!mutedUsersPath.getParent().toFile().exists()) mutedUsersPath.getParent().toFile().mkdirs();
            FileUtils.writeStringToFile(mutedUsersPath.toFile(), gson.toJson(mutedUsers), Charset.defaultCharset());
        } catch (IOException e)
        {
            MineTogetherCommon.sentryException(e);
        }
    }

    public static void unmuteUser(String longhash)
    {
        try
        {
            mutedUsers.remove(longhash);
            CompletableFuture.runAsync(() ->
            {
                Profile profile = KnownUsers.findByHash(longhash);
                if (profile == null) profile = KnownUsers.add(longhash);
                try
                {
                    profile.loadProfile();
                } catch (IOException e)
                {
                    Sentry.captureException(e);
                }
                profile.setMuted(false);
                KnownUsers.update(profile);
            }, MineTogetherChat.profileExecutor);
        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try
        {
            if (!mutedUsersPath.getParent().toFile().exists()) mutedUsersPath.getParent().toFile().mkdirs();
            FileUtils.writeStringToFile(mutedUsersPath.toFile(), gson.toJson(mutedUsers), Charset.defaultCharset());
        } catch (IOException e)
        {
            MineTogetherCommon.sentryException(e);
        }
    }

    public static void loadMutedList()
    {
        if(!mutedUsersPath.toFile().exists()) return;

        Gson gson = new Gson();
        try
        {
            FileReader fileReader = new FileReader(mutedUsersPath.toFile());
            mutedUsers = gson.fromJson(fileReader, ArrayList.class);
            for (String s : mutedUsers)
            {
                CompletableFuture.runAsync(() ->
                {
                    Profile profile = KnownUsers.findByHash(s);
                    if (profile == null) profile = KnownUsers.add(s);
                    try
                    {
                        profile.loadProfile();
                    } catch (IOException e)
                    {
                        Sentry.captureException(e);
                    }
                    profile.setMuted(true);
                    KnownUsers.update(profile);
                }, MineTogetherChat.profileExecutor);
            }
        } catch (Exception e)
        {
            MineTogetherCommon.sentryException(e);
        }
    }

    public static MineTogetherChat getMineTogetherChat()
    {
        return mineTogetherChat;
    }
}
