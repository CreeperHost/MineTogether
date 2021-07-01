package net.creeperhost.minetogether.module.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.architectury.hooks.ScreenHooks;
import me.shedaniel.architectury.platform.Platform;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.mixin.ChatComponentInvoker;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogether.threads.FriendUpdateThread;
import net.creeperhost.minetogether.verification.ModPackVerifier;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.KnownUsers;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.io.FileUtils;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatModule
{
    public static boolean showMTChat = false;
    public static boolean hasNewMessage = true;
    public static ArrayList<String> mutedUsers = new ArrayList<>();
    public static Path mutedUsersPath = Platform.getGameFolder().resolve("local/minetogether/mutedusers.json");
    private static MineTogetherChat mineTogetherChat;

    public static void init()
    {
        String ourNick = "MT" + ChatCallbacks.getPlayerHash(MineTogetherClient.getUUID()).substring(0, 28);
        FriendUpdateThread.init();
        buildChat(ourNick);
        loadMutedList();
    }

    public static void buildChat(String ourNick)
    {
        MineTogether.logger.info("Building MineTogether chat");
        UUID uuid = MineTogetherClient.getUUID();
        boolean online = MineTogetherClient.isOnlineUUID;
        String realName = new ModPackVerifier().verify();
        String signature = new SignatureVerifier().verify();
        String serverID = MineTogetherClient.getServerIDAndVerify();

        mineTogetherChat = new MineTogetherChat(ourNick, uuid, online, realName, signature, serverID);
        mineTogetherChat.startChat();
    }

    public static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
    {
        Button friendsButton = new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether.multiplayer.friends"), p ->
                Minecraft.getInstance().setScreen(new FriendsListScreen(screen)));

        Button chatButton = new ButtonMultiple(screen.width - 125, 5, 1, Constants.WIDGETS_LOCATION, p ->
                Minecraft.getInstance().setScreen(Config.getInstance().isChatEnabled() ? new ChatScreen(screen) : new SettingsScreen(screen)));

        if (screen instanceof TitleScreen && Config.getInstance().isEnableMainMenuFriends())
        {
            if(Config.instance.isMainMenuEnabled())
            {
                ScreenHooks.addButton(screen, friendsButton);
                ScreenHooks.addButton(screen, chatButton);
            }
        }
        if(screen instanceof PauseScreen)
        {
            ScreenHooks.addButton(screen, friendsButton);
            ScreenHooks.addButton(screen, chatButton);
        }
    }

    public static void sendMessage(Component component)
    {
        if(ChatModule.showMTChat)
            ((ChatComponentInvoker) Minecraft.getInstance().gui.getChat()).invokeAddMessage(component, 0, Minecraft.getInstance().gui.getGuiTicks(), false);
    }

    public static void muteUser(String user)
    {
        //Don't add the user if they are already in the list
        if(mutedUsers.contains(user)) return;

        mutedUsers.add(user);
        CompletableFuture.runAsync(() -> {
            Profile profile = KnownUsers.findByHash(user);
            if(profile == null) profile = KnownUsers.add(user);
            profile.loadProfile();
            profile.setMuted(true);
            KnownUsers.update(profile);
        }, MineTogetherChat.profileExecutor);

        Gson gson = new Gson();
        try
        {
            if(!mutedUsersPath.getParent().toFile().exists()) mutedUsersPath.getParent().toFile().mkdirs();
            FileUtils.writeStringToFile(mutedUsersPath.toFile(), gson.toJson(mutedUsers), Charset.defaultCharset());
        } catch (IOException ignored) {}
    }

    public static void unmuteUser(String longhash)
    {
        try
        {
            mutedUsers.remove(longhash);
            CompletableFuture.runAsync(() -> {
                Profile profile = KnownUsers.findByHash(longhash);
                if(profile == null) profile = KnownUsers.add(longhash);
                profile.loadProfile();
                profile.setMuted(false);
                KnownUsers.update(profile);
            }, MineTogetherChat.profileExecutor);
        } catch (Exception ignored) {}
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try
        {
            if(!mutedUsersPath.getParent().toFile().exists()) mutedUsersPath.getParent().toFile().mkdirs();
            FileUtils.writeStringToFile(mutedUsersPath.toFile(), gson.toJson(mutedUsers), Charset.defaultCharset());
        } catch (IOException ignored) {}
    }

    public static void loadMutedList()
    {
        Gson gson = new Gson();
        try
        {
            FileReader fileReader = new FileReader(mutedUsersPath.toFile());
            mutedUsers = gson.fromJson(fileReader, ArrayList.class);
            for(String s : mutedUsers)
            {
                CompletableFuture.runAsync(() -> {
                    Profile profile = KnownUsers.findByHash(s);
                    if(profile == null) profile = KnownUsers.add(s);
                    profile.loadProfile();
                    profile.setMuted(true);
                    KnownUsers.update(profile);
                }, MineTogetherChat.profileExecutor);
            }
        } catch (Exception ignored)
        {
        }
    }

    public static MineTogetherChat getMineTogetherChat()
    {
        return mineTogetherChat;
    }
}
