package net.creeperhost.minetogether.events;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.PrivateChat;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.serverlist.data.Invite;
import net.creeperhost.minetogether.client.screen.serverlist.gui.FriendsListScreen;
import net.creeperhost.minetogether.client.screen.serverlist.gui.InvitedScreen;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.oauth.ServerAuthTest;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.proxy.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClientTickEvents
{
    private static final Logger logger = LogManager.getLogger();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    Minecraft mc = Minecraft.getInstance();
    private Thread inviteCheckThread;
    private int inviteTicks = -1;

    private int clientTicks = 0; //Tick counter client side.
    private UUID lastUUID; //Last UUID we saw the client using.
    public static boolean connectToChat = false; //If a connection is scheduled
    public static boolean disconnectFromChat = false; //If a disconnection is scheduled
    public static boolean chatDisconnected = false; //If we know we are disconnected.

    private static Future<?> onlineCheckFuture;
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent evt)
    {
        if (evt.phase == TickEvent.Phase.START)
        {
            if (clientTicks % (10 * 20) == 0 && MineTogether.instance.gdpr.hasAcceptedGDPR() && Config.getInstance().isChatEnabled()) { //Every second is bad, Bad bad Covers
                MineTogether.proxy.reCacheUUID(); //Careful with this, recomputes the GameProfile
                UUID currentUUID = MineTogether.proxy.getUUID();
                if (lastUUID == null)
                {
                    lastUUID = currentUUID;
                }
                if (!lastUUID.equals(currentUUID))
                {
                    if (onlineCheckFuture == null || onlineCheckFuture.isDone())
                    {
                        onlineCheckFuture = executor.submit(() -> {
                            MineTogether.isOnline = MineTogether.proxy.checkOnline();
                        });
                    }
                    if (currentUUID.version() != 4)
                    {
                        disconnectFromChat = true;
                    }
                } else
                {
                    connectToChat = true;
                }
                lastUUID = currentUUID;
            }

            //Try and disconnect if we have been told to.
            if (disconnectFromChat && ChatHandler.connectionStatus == ChatHandler.ConnectionStatus.DISCONNECTED) {
                ChatHandler.requestReconnect();
                chatDisconnected = true;
            }
            connectToChat = false;
            disconnectFromChat = false;
        }
        if (evt.phase == TickEvent.Phase.END) {
            clientTicks++;
        }
        if (!MineTogether.isOnline) return;
        ServerAuthTest.processPackets();

        inviteTicks = (inviteTicks + 1) % 300;
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
                            if(ChatHandler.isOnline()) //No point in trying this without a connection
                            {
                                tempInvite = Callbacks.getInvite();
                                temp = ChatHandler.privateChatInvite;

                                synchronized (MineTogether.instance.inviteLock) {
                                    if (tempInvite != null)
                                        MineTogether.instance.invite = tempInvite;
                                }

                                if (temp != null) {
                                    MineTogether.instance.toastHandler.displayToast(new StringTextComponent(I18n.format("Your friend %s invited you to a private chat", MineTogether.instance.getNameForUser(temp.getOwner()), ((Client) MineTogether.proxy).openGuiKey.getTranslationKey())), 10000, () -> {
                                        mc.displayGuiScreen(new MTChatScreen(Minecraft.getInstance().currentScreen, true));
                                    });
                                }
                            }
                        } catch (Exception ignored)
                        {
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
                if (mc.currentScreen != null && mc.currentScreen instanceof FriendsListScreen)
                {
                    MineTogether.proxy.openFriendsGui();
                } else
                {
                    MineTogether.instance.toastHandler.displayToast(new StringTextComponent(I18n.format("creeperhost.multiplayer.invitetoast", ((Client) MineTogether.proxy).openGuiKey.getTranslationKey())), 10000, () ->
                    {
                        mc.displayGuiScreen(new InvitedScreen(MineTogether.instance.handledInvite, mc.currentScreen));
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
                if (friendMessage && Minecraft.getInstance().currentScreen instanceof MTChatScreen)
                    return;
                if (Config.getInstance().isFriendOnlineToastsEnabled())
                {
                    MineTogether.instance.toastHandler.displayToast(new StringTextComponent(I18n.format(friendMessage ? "%s has sent you a message!" : "Your friend %s has come online!", friend)), 4000, null);
                }
            }
        }
    }
}
