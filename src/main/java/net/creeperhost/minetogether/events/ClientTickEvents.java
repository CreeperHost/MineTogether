package net.creeperhost.minetogether.events;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.PrivateChat;
import net.creeperhost.minetogether.client.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.client.gui.serverlist.data.Invite;
import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.lib.ModInfo;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.proxy.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientTickEvents
{
    Minecraft mc = Minecraft.getInstance();
    private Thread inviteCheckThread;
    private int inviteTicks = -1;
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent evt)
    {
        inviteTicks = (inviteTicks + 1) % 20;
        if (inviteTicks != 0)
            return;
        
        if (Config.getInstance().isServerListEnabled() && MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            if (inviteCheckThread == null)
            {
                inviteCheckThread = new Thread(() ->
                {
                    while (Config.getInstance().isServerListEnabled())
                    {
                        Invite tempInvite = null;
                        PrivateChat temp = null;
                        
                        try
                        {
                            tempInvite = Callbacks.getInvite();
                            temp = ChatHandler.privateChatInvite;
                            
                            synchronized (MineTogether.instance.inviteLock)
                            {
                                if (tempInvite != null)
                                    MineTogether.instance.invite = tempInvite;
                            }
                            
                            if (temp != null)
                            {
                                ToastHandler.displayToast(I18n.format("Your friend %s invited you to a private chat", MineTogether.instance.getNameForUser(temp.getOwner()), ((Client) MineTogether.proxy).openGuiKey.getTranslationKey()), 10000, () ->
                                {
                                    mc.displayGuiScreen(new GuiMTChat(Minecraft.getInstance().currentScreen, true));
                                });
                            }
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                            // carry on - we'll just try again later, saves thread dying.
                        }
                        
                        try
                        {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored)
                        {
                        }
                    }
                });
                inviteCheckThread.setDaemon(true);
                inviteCheckThread.setName("MineTogether invite check thread");
                inviteCheckThread.start();
            }
            
            boolean handled = false;
            synchronized (MineTogether.instance.inviteLock)
            {
                if (MineTogether.instance.invite != null)
                {
                    MineTogether.instance.handledInvite = MineTogether.instance.invite;
                    MineTogether.instance.invite = null;
                    
                    handled = true;
                }
            }
            
            if (handled)
            {
                ArrayList<Friend> friendsList = Callbacks.getFriendsList(true);
                String friendName = "Unknown";
                
                for (Friend friend : friendsList)
                {
                    if (friend.getCode().equals(MineTogether.instance.handledInvite.by))
                    {
                        friendName = friend.getName();
                        MineTogether.instance.handledInvite.by = friendName;
                        break;
                    }
                }
                if (mc.currentScreen != null && mc.currentScreen instanceof GuiFriendsList)
                {
                    MineTogether.proxy.openFriendsGui();
                } else
                {
                    ToastHandler.displayToast(I18n.format("creeperhost.multiplayer.invitetoast", ((Client) MineTogether.proxy).openGuiKey.getTranslationKey()), 10000, () ->
                    {
//                        mc.displayGuiScreen(new GuiInvited(MineTogether.instance.handledInvite, mc.currentScreen));
                        MineTogether.instance.handledInvite = null;
                    });
                }
            }
        }
        
        if (Config.getInstance().isChatEnabled())
        {
            String friend;
            boolean friendMessage;
            
            synchronized (MineTogether.instance.friendLock)
            {
                friend = MineTogether.instance.friend;
                friendMessage = MineTogether.instance.friendMessage;
                MineTogether.instance.friend = null;
            }
            
            if (friend != null)
            {
                if (friendMessage && Minecraft.getInstance().currentScreen instanceof GuiMTChat)
                    return;
                if (Config.getInstance().isFriendOnlineToastsEnabled())
                {
                    ToastHandler.displayToast(I18n.format(friendMessage ? "%s has sent you a message!" : "Your friend %s has come online!", friend), 4000, null);
                }
            }
        }
    }
}
