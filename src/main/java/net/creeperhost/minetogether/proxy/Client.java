package net.creeperhost.minetogether.proxy;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.EventHandler;
import net.creeperhost.minetogether.chat.ChatConnectionHandler;
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
import net.creeperhost.minetogether.irc.IrcHandler;
import net.creeperhost.minetogether.misc.Callbacks;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Session;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Client implements IProxy
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new GsonBuilder().create();
    private static final Type strListToken = new TypeToken<List<String>>() {}.getType();
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

        UUID uuid = session.getProfile().getId();

        CreeperHost.instance.online = uuid.version() == 4;//Version 3 uuids are offline ones, aka, name uuid.
        
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
        if(!EventHandler.isOnline) return;

        if (Config.getInstance().isChatEnabled())
        {
            if (!CreeperHost.instance.ingameChat.hasDisabledIngameChat())
                enableIngameChat();

            CreeperHost.instance.ourNick = "MT" + Callbacks.getPlayerHash(CreeperHost.proxy.getUUID()).substring(0, 28);
            CreeperHost.instance.playerName = Minecraft.getMinecraft().getSession().getUsername();
            CreeperHost.instance.getNameForUser("");
            CreeperHost.instance.mutedUsersFile = new File("local/minetogether/mutedusers.json");

            CreeperHost.mutedUsers = new ArrayList<>();
            if (CreeperHost.instance.mutedUsersFile.exists()) {
                try (FileInputStream fis = new FileInputStream(CreeperHost.instance.mutedUsersFile)) {
                    CreeperHost.mutedUsers = gson.fromJson(new InputStreamReader(fis), strListToken);
                } catch (IOException ignored) { }
            }
            ChatHandler.init(CreeperHost.instance.ourNick, CreeperHost.instance.realName, CreeperHost.instance.online, CreeperHost.instance);
        }
    }

    @Override
    public void stopChat()
    {
        IrcHandler.stop(true);
    }
    
    @Override
    public void disableIngameChat()
    {
        CreeperHost.instance.ingameChat.setDisabledIngameChat(true);
        if (isChatReplaced)
        {
            isChatReplaced = false;
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
    public void closeGroupChat()
    {
        ChatHandler.closePrivateChat();
        GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
        chatGUI.setBase(true);
        chatGUI.rebuildChat(ChatHandler.CHANNEL);
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen != null)
        {
            if (currentScreen instanceof GuiChatOurs)
                currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), currentScreen.width, currentScreen.height);
            if (currentScreen instanceof GuiMTChat)
            {
                for(DropdownButton.IDropdownOption target: Target.getMainTarget().getPossibleVals())
                {
                    if (((Target)target).getInternalTarget().equals(ChatHandler.CHANNEL))
                    {
                        ((GuiMTChat) currentScreen).targetDropdownButton.setSelected((Target) target);
                        Target.updateCache();
                    }
                }
                currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), currentScreen.width, currentScreen.height);
            }
        }
    }

    @Override
    public void messageReceived(String target, Message messagePair)
    {
        if (!Config.getInstance().isChatEnabled() || (!target.toLowerCase().equals(ChatHandler.CHANNEL.toLowerCase()) && !target.toLowerCase().equals(ChatHandler.currentGroup.toLowerCase())) || !(Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs))
            return;
        GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
        if (target.toLowerCase().equals(ourChat.chatTarget.toLowerCase()))
            ourChat.setChatLine(messagePair, GuiMTChat.formatLine(messagePair), 0, Minecraft.getMinecraft().ingameGUI.getUpdateCounter(), false);
    }

    @Override
    public void updateChatChannel()
    {
        if (Config.getInstance().isChatEnabled() && Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
        {
            GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
            if (chatGUI.chatTarget.equals("#MineTogether"))
                chatGUI.chatTarget = ChatHandler.CHANNEL;
        }
    }

    @Override
    public void refreshChat()
    {
        if (Config.getInstance().isChatEnabled() && Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
        {
            GuiNewChatOurs chatGUI = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
            if (!chatGUI.isBase())
                chatGUI.rebuildChat(chatGUI.chatTarget);
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (currentScreen != null && currentScreen instanceof GuiMTChat)
                ((GuiMTChat)currentScreen).rebuildChat(true);
        }
    }

    @Override
    public boolean checkOnline()
    {
        YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
        YggdrasilMinecraftSessionService sessionService = (YggdrasilMinecraftSessionService) authService.createMinecraftSessionService();
        Session session = Minecraft.getMinecraft().getSession();
        GameProfile profile = session.getProfile();
        String token = session.getToken();
        String serverId = UUID.randomUUID().toString();
        try
        {
            sessionService.joinServer(profile, token, serverId);
            GameProfile gameProfile = sessionService.hasJoinedServer(profile, serverId, null);
            return gameProfile != null && gameProfile.isComplete();
        } catch (AuthenticationException ignored) {}
        return false;
    }
}
