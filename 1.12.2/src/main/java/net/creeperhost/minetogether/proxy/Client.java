package net.creeperhost.minetogether.proxy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.gui.chat.Target;
import net.creeperhost.minetogether.gui.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.gui.chat.ingame.GuiNewChatOurs;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiInvited;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Session;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class Client implements IProxy
{
    public KeyBinding openGuiKey;
    private UUID cache;
    
    @Override
    public void registerKeys()
    {
        openGuiKey = new KeyBinding("minetogether.key.friends", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_M, "minetogether.keys");
        ClientRegistry.registerKeyBinding(openGuiKey);
    }
    
    @Override
    public void openFriendsGui()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (CreeperHost.instance.handledInvite == null)
        {
            mc.displayGuiScreen(new GuiFriendsList(mc.currentScreen));
        } else
        {
            mc.displayGuiScreen(new GuiInvited(CreeperHost.instance.handledInvite, mc.currentScreen));
            CreeperHost.instance.handledInvite = null;
        }
    }
    
    @Override
    public UUID getUUID()
    {
        if (cache != null)
            return cache;

        Session session = Minecraft.getMinecraft().getSession();

        UUID uuid = Minecraft.getMinecraft().getSession().getProfile().getId();

        CreeperHost.instance.online = !uuid.equals(EntityPlayer.getOfflineUUID(session.getUsername()));
        
        cache = uuid;

        return uuid;
    }
    
    boolean isChatReplaced = false;
    
    @Override
    public void startChat()
    {
        if (Config.getInstance().isChatEnabled())
        {
            
            if (!CreeperHost.instance.ingameChat.hasDisabledIngameChat())
                enableIngameChat();
            
            CreeperHost.instance.getNameForUser("");
            CreeperHost.instance.mutedUsersFile = new File("local/minetogether/mutedusers.json");
            InputStream mutedUsersStream = null;
            try
            {
                String configString;
                if (CreeperHost.instance.mutedUsersFile.exists())
                {
                    mutedUsersStream = new FileInputStream(CreeperHost.instance.mutedUsersFile);
                    configString = IOUtils.toString(mutedUsersStream);
                } else
                {
                    CreeperHost.instance.mutedUsersFile.getParentFile().mkdirs();
                    configString = "[]";
                }
                
                Gson gson = new Gson();
                CreeperHost.instance.mutedUsers = gson.fromJson(configString, new TypeToken<List<String>>() {}.getType());
            } catch (Throwable ignored) {}
            finally
            {
                try
                {
                    if (mutedUsersStream != null)
                    {
                        mutedUsersStream.close();
                    }
                } catch (Throwable ignored) {}
            }
            new Thread(() -> ChatHandler.init(CreeperHost.instance.ourNick, CreeperHost.instance.realName, CreeperHost.instance.online, CreeperHost.instance)).start(); // start in thread as can hold up the UI thread for some reason.
        }
    }
    
    @Override
    public void disableIngameChat()
    {
        CreeperHost.instance.ingameChat.setDisabledIngameChat(true);
        if (isChatReplaced)
        {
            ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).setBase(true); // don't actually remove
        }
    }
    
    @Override
    public void enableIngameChat()
    {
        CreeperHost.instance.ingameChat.setDisabledIngameChat(false);
        
        if (!isChatReplaced)
        {
            isChatReplaced = true;
            try
            {
                Field field = ReflectionHelper.findField(GuiIngame.class, "persistantChatGUI", "field_73840_e", "");
                field.set(Minecraft.getMinecraft().ingameGUI, new GuiNewChatOurs(Minecraft.getMinecraft()));
            } catch (IllegalAccessException ignored) {}
        }
    }

    @Override
    public void closeGroupChat() {
        ChatHandler.closePrivateChat();
        GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
        chatGUI.setBase(true);
        chatGUI.rebuildChat(ChatHandler.CHANNEL);
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen != null) {
            if (currentScreen instanceof GuiChatOurs)
                currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), currentScreen.width, currentScreen.height);
            if (currentScreen instanceof GuiMTChat) {
                for(DropdownButton.IDropdownOption target: Target.getMainTarget().getPossibleVals())
                {
                    if (((Target)target).getInternalTarget().equals(ChatHandler.CHANNEL)) {
                        ((GuiMTChat) currentScreen).targetDropdownButton.setSelected((Target) target);
                        Target.updateCache();
                    }
                }
                currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), currentScreen.width, currentScreen.height);
            }
        }
    }

    @Override
    public void messageReceived(String target, Message messagePair) {
        if (!Config.getInstance().isChatEnabled() || (!target.toLowerCase().equals(ChatHandler.CHANNEL.toLowerCase()) && !target.toLowerCase().equals(ChatHandler.currentGroup.toLowerCase())) || !(Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs))
            return;
        GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
        if (target.toLowerCase().equals(ourChat.chatTarget.toLowerCase()))
            ourChat.setChatLine(GuiMTChat.formatLine(messagePair), 0, Minecraft.getMinecraft().ingameGUI.getUpdateCounter(), false);
    }

    @Override
    public void updateChatChannel() {
        if (Config.getInstance().isChatEnabled() && Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
        {
            GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
            if (chatGUI.chatTarget.equals("#MineTogether"))
                chatGUI.chatTarget = ChatHandler.CHANNEL;
        }
    }

    @Override
    public void refreshChat() {
        if (Config.getInstance().isChatEnabled() && Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) {
            GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
            if (!chatGUI.isBase())
                chatGUI.rebuildChat(chatGUI.chatTarget);
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (currentScreen != null && currentScreen instanceof GuiMTChat)
                ((GuiMTChat)currentScreen).rebuildChat();
        }
    }

    @Override
    public boolean checkOnline() {
        YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
        YggdrasilMinecraftSessionService sessionService = (YggdrasilMinecraftSessionService) authService.createMinecraftSessionService();
        Session session = Minecraft.getMinecraft().getSession();
        GameProfile profile = session.getProfile();
        String token = session.getToken();
        String serverId = UUID.randomUUID().toString();
        try {
            sessionService.joinServer(profile, token, serverId);
            GameProfile gameProfile = sessionService.hasJoinedServer(profile, token, null);
            return gameProfile != null && gameProfile.isComplete();
        } catch (AuthenticationException ignored) {}
        return false;
    }
}
