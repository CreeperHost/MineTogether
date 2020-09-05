package net.creeperhost.minetogether.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatConnectionHandler;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.chat.Target;
import net.creeperhost.minetogether.client.screen.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.client.screen.chat.ingame.GuiNewChatOurs;
import net.creeperhost.minetogether.client.screen.element.DropdownButton;
import net.creeperhost.minetogether.client.screen.serverlist.gui.FriendsListScreen;
import net.creeperhost.minetogether.client.screen.serverlist.gui.InvitedScreen;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Session;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Client implements IProxy
{
    private static final Gson gson = new GsonBuilder().create();
    private static final Type strListToken = new TypeToken<List<String>>() {}.getType();
    public KeyBinding openGuiKey;
    private UUID cache;
    public static int chatType = 0;
    public static boolean first = true;

    @Override
    public void registerKeys()
    {
        //TODO
        openGuiKey = new KeyBinding("minetogether.key.friends", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_M, "minetogether.keys");
        ClientRegistry.registerKeyBinding(openGuiKey);
    }
    
    @Override
    public void openFriendsGui()
    {
        Minecraft mc = Minecraft.getInstance();
        if (MineTogether.instance.handledInvite == null)
        {
            mc.displayGuiScreen(new FriendsListScreen(mc.currentScreen));
        } else
        {
            mc.displayGuiScreen(new InvitedScreen(MineTogether.instance.handledInvite, mc.currentScreen));
            MineTogether.instance.handledInvite = null;
        }
    }
    
    @Override
    public UUID getUUID()
    {
        if (cache != null)
            return cache;

        Session session = Minecraft.getInstance().getSession();

        UUID uuid = Minecraft.getInstance().getSession().getProfile().getId();

        MineTogether.instance.online = !uuid.equals(PlayerEntity.getOfflineUUID(session.getUsername()));

        cache = uuid;

        return uuid;
    }

    @Override
    public void reCacheUUID()
    {
        cache = null;
    }
    
    boolean isChatReplaced = false;
    
    @Override
    public void startChat()
    {
        if(!MineTogether.isOnline) return;

        if (Config.getInstance().isChatEnabled())
        {
            if (!MineTogether.instance.ingameChat.hasDisabledIngameChat())
                enableIngameChat();

            MineTogether.instance.ourNick = "MT" + Callbacks.getPlayerHash(MineTogether.proxy.getUUID()).substring(0, 28);
            MineTogether.instance.playerName = Minecraft.getInstance().getSession().getUsername();
            MineTogether.instance.getNameForUser("");
            MineTogether.instance.mutedUsersFile = new File("local/minetogether/mutedusers.json");

            MineTogether.mutedUsers = new ArrayList<>();
            if (MineTogether.instance.mutedUsersFile.exists()) {
                try (FileInputStream fis = new FileInputStream(MineTogether.instance.mutedUsersFile)) {
                    MineTogether.mutedUsers = gson.fromJson(new InputStreamReader(fis), strListToken);
                } catch (IOException ignored) { }
            }
            CompletableFuture.runAsync(() -> ChatHandler.init(MineTogether.instance.ourNick, MineTogether.instance.realName, MineTogether.instance.online, MineTogether.instance), MineTogether.profileExecutor); // start in thread as can hold up the UI thread for some reason.
        }
    }

    @Override
    public void stopChat()
    {
        ChatConnectionHandler.INSTANCE.disconnect();
    }
    
    @Override
    public void disableIngameChat()
    {
        MineTogether.instance.ingameChat.setDisabledIngameChat(true);
        if (isChatReplaced)
        {
            isChatReplaced = false;
            ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setBase(true); // don't actually remove
        }
    }
    
    @Override
    public void enableIngameChat()
    {
        MineTogether.instance.ingameChat.setDisabledIngameChat(false);
        
        if (!isChatReplaced)
        {
            isChatReplaced = true;
            IngameGui ingameGui = Minecraft.getInstance().ingameGUI;
            ingameGui.persistantChatGUI = new GuiNewChatOurs(Minecraft.getInstance());
        }
    }
    
    @Override
    public void closeGroupChat()
    {
        ChatHandler.closePrivateChat();
        GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
        chatGUI.setBase(true);
        chatGUI.rebuildChat(ChatHandler.CHANNEL);
        Screen currentScreen = Minecraft.getInstance().currentScreen;
        if (currentScreen != null)
        {
            if (currentScreen instanceof GuiChatOurs)
                currentScreen.setSize(currentScreen.width, currentScreen.height);
            if (currentScreen instanceof MTChatScreen)
            {
                for (DropdownButton.IDropdownOption target : Target.getMainTarget().getPossibleVals())
                {
                    if (((Target) target).getInternalTarget().equals(ChatHandler.CHANNEL))
                    {
                        ((MTChatScreen) currentScreen).targetDropdownButton.setSelected((Target) target);
                        Target.updateCache();
                    }
                }
                currentScreen.setSize(currentScreen.width, currentScreen.height);
            }
        }
    }
    
    @Override
    public void messageReceived(String target, Message messagePair)
    {
        if (!Config.getInstance().isChatEnabled() || (!target.toLowerCase().equals(ChatHandler.CHANNEL.toLowerCase()) && !target.toLowerCase().equals(ChatHandler.currentGroup.toLowerCase())) || !(Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs))
            return;
        GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
        if (target.toLowerCase().equals(ourChat.chatTarget.toLowerCase()))
            ourChat.setChatLine(messagePair, Objects.requireNonNull(MTChatScreen.formatLine(messagePair)), 0, Minecraft.getInstance().ingameGUI.getTicks(), false);
    }
    
    @Override
    public void updateChatChannel()
    {
        if (Config.getInstance().isChatEnabled() && Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
        {
            GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
            if (chatGUI.chatTarget.equals("#MineTogether"))
                chatGUI.chatTarget = ChatHandler.CHANNEL;
        }
    }
    
    @Override
    public void refreshChat()
    {
        if (Config.getInstance().isChatEnabled() && Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
        {
            GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
            if (!chatGUI.isBase())
                chatGUI.rebuildChat(chatGUI.chatTarget);
            Screen currentScreen = Minecraft.getInstance().currentScreen;
            if (currentScreen != null && currentScreen instanceof MTChatScreen)
                ((MTChatScreen) currentScreen).rebuildChat();
        }
    }
    
    @Override
    public boolean checkOnline()
    {
        YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy(), UUID.randomUUID().toString());
        YggdrasilMinecraftSessionService sessionService = (YggdrasilMinecraftSessionService) authService.createMinecraftSessionService();
        Session session = Minecraft.getInstance().getSession();
        GameProfile profile = session.getProfile();
        String token = session.getToken();
        String serverId = UUID.randomUUID().toString();
        try
        {
            sessionService.joinServer(profile, token, serverId);
            GameProfile gameProfile = sessionService.hasJoinedServer(profile, serverId, null);
            return gameProfile != null && gameProfile.isComplete();
        } catch (AuthenticationException ignored)
        {
        }
        return false;
    }
}
