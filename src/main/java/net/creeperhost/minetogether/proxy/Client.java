package net.creeperhost.minetogether.proxy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.client.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.client.gui.chat.Target;
import net.creeperhost.minetogether.client.gui.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.client.gui.chat.ingame.GuiNewChatOurs;
import net.creeperhost.minetogether.client.gui.element.DropdownButton;
import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiInvited;
import net.creeperhost.minetogether.common.IngameChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.events.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Session;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Client implements IProxy
{
    public KeyBinding openGuiKey;
    private UUID cache;
    
    @Override
    public void registerKeys()
    {
        //TODO
//        openGuiKey = new KeyBinding("minetogether.key.friends", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_M, "minetogether.keys");
//        ClientRegistry.registerKeyBinding(openGuiKey);
    }
    
    @Override
    public void openFriendsGui()
    {
        Minecraft mc = Minecraft.getInstance();
        if (MineTogether.instance.handledInvite == null)
        {
            mc.displayGuiScreen(new GuiFriendsList(mc.currentScreen));
        } else
        {
            mc.displayGuiScreen(new GuiInvited(MineTogether.instance.handledInvite, mc.currentScreen));
            MineTogether.instance.handledInvite = null;
        }
    }
    
    @Override
    public UUID getUUID()
    {
        if (cache != null)
            return cache;
        Minecraft mc = Minecraft.getInstance();
        Session session = mc.getSession();
        boolean online = MineTogether.instance.online;
        
        UUID uuid;
        
        if (online)
        {
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(mc.getProxy(), UUID.randomUUID().toString());
            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(mc.gameDir, MinecraftServer.USER_CACHE_FILE.getName()));
            uuid = playerprofilecache.getGameProfileForUsername(Minecraft.getInstance().getSession().getUsername()).getId();
        } else
        {
            uuid = PlayerEntity.getOfflineUUID(session.getUsername().toLowerCase());
        }
        cache = uuid;
        
        MineTogether.instance.online = online;
        return uuid;
    }
    
    boolean isChatReplaced = false;
    
    @Override
    public void startChat()
    {
        if (Config.getInstance().isChatEnabled())
        {
            
            if (!MineTogether.instance.ingameChat.hasDisabledIngameChat())
                enableIngameChat();
            
            MineTogether.instance.getNameForUser("");
            MineTogether.instance.mutedUsersFile = new File("local/minetogether/mutedusers.json");
            InputStream mutedUsersStream = null;
            try
            {
                String configString;
                if (MineTogether.instance.mutedUsersFile.exists())
                {
                    mutedUsersStream = new FileInputStream(MineTogether.instance.mutedUsersFile);
                    configString = IOUtils.toString(mutedUsersStream);
                } else
                {
                    MineTogether.instance.mutedUsersFile.getParentFile().mkdirs();
                    configString = "[]";
                }
                
                Gson gson = new Gson();
                MineTogether.instance.mutedUsers = gson.fromJson(configString, new TypeToken<List<String>>()
                {
                }.getType());
            } catch (Throwable ignored)
            {
            } finally
            {
                try
                {
                    if (mutedUsersStream != null)
                    {
                        mutedUsersStream.close();
                    }
                } catch (Throwable ignored)
                {
                }
            }
            new Thread(() -> ChatHandler.init(MineTogether.instance.ourNick, MineTogether.instance.realName, MineTogether.instance.online, MineTogether.instance)).start(); // start in thread as can hold up the UI thread for some reason.
        }
    }
    
    @Override
    public void disableIngameChat()
    {
        MineTogether.instance.ingameChat.setDisabledIngameChat(true);
        if (isChatReplaced)
        {
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
            if (currentScreen instanceof GuiMTChat)
            {
                for (DropdownButton.IDropdownOption target : Target.getMainTarget().getPossibleVals())
                {
                    if (((Target) target).getInternalTarget().equals(ChatHandler.CHANNEL))
                    {
                        ((GuiMTChat) currentScreen).targetDropdownButton.setSelected((Target) target);
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
            ourChat.setChatLine(Objects.requireNonNull(GuiMTChat.formatLine(messagePair)), 0, Minecraft.getInstance().ingameGUI.getTicks(), false);
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
            if (currentScreen != null && currentScreen instanceof GuiMTChat)
                ((GuiMTChat) currentScreen).rebuildChat();
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
            GameProfile gameProfile = sessionService.hasJoinedServer(profile, token, null);
            return gameProfile != null && gameProfile.isComplete();
        } catch (AuthenticationException ignored)
        {
        }
        return false;
    }
}
