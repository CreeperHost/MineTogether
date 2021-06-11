package net.creeperhost.minetogether.module.chat;

import com.google.gson.Gson;
import me.shedaniel.architectury.hooks.ScreenHooks;
import me.shedaniel.architectury.platform.Platform;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.mixin.ChatComponentInvoker;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogether.verification.ModPackVerifier;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
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

public class ChatModule
{
    public static boolean showMTChat = false;
    public static boolean hasNewMessage = true;
    public static ArrayList<String> mutedUsers = new ArrayList<>();
    public static Path mutedUsersPath = Platform.getGameFolder().resolve("local/minetogether/mutedusers.json");
    private static MineTogetherChat mineTogetherChat;

    public static void init()
    {
        loadMutedList();
        buildChat();
    }

    public static void buildChat()
    {
        MineTogether.logger.info("Building MineTogether chat");
        String ourNick = "MT" + ChatCallbacks.getPlayerHash(MineTogetherClient.getUUID()).substring(0, 28);
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
        if (screen instanceof TitleScreen)
        {
            if(Config.getInstance().isEnableMainMenuFriends())
            {
                ScreenHooks.addButton(screen, new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether.multiplayer.friends"), p ->
                        Minecraft.getInstance().setScreen(new FriendsListScreen(screen))));

                ScreenHooks.addButton(screen, new ButtonMultiple(screen.width - 125, 5, 1, p ->
                {
                    if (Config.getInstance().isChatEnabled()) {
                        Minecraft.getInstance().setScreen(new ChatScreen(screen));
                    } else {
                        Minecraft.getInstance().setScreen(new SettingsScreen(screen));
                    }
                }));
            }
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
        Gson gson = new Gson();
        try
        {
            if(!mutedUsersPath.getParent().toFile().exists()) mutedUsersPath.getParent().toFile().mkdirs();
            FileUtils.writeStringToFile(mutedUsersPath.toFile(), gson.toJson(mutedUsers), Charset.defaultCharset());
        } catch (IOException ignored) {}
    }

    public static void unmuteUser(String user)
    {
        Profile profile = ChatHandler.knownUsers.findByDisplay(user);
        try
        {
            mutedUsers.remove(user);
            mutedUsers.remove(profile.getShortHash());
            mutedUsers.remove(profile.getMediumHash());
        } catch (Exception ignored) {}
        Gson gson = new Gson();
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
        } catch (Exception ignored)
        {
        }
    }

    public static MineTogetherChat getMineTogetherChat()
    {
        return mineTogetherChat;
    }
}
