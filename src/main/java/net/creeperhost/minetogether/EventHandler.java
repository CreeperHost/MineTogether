package net.creeperhost.minetogether;

import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.aries.Aries;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.PrivateChat;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.gui.*;
import net.creeperhost.minetogether.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.gui.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.gui.chat.ingame.GuiNewChatOurs;
import net.creeperhost.minetogether.gui.element.GuiButtonCreeper;
import net.creeperhost.minetogether.gui.element.GuiButtonMultiple;
import net.creeperhost.minetogether.gui.mpreplacement.CreeperHostServerSelectionList;
import net.creeperhost.minetogether.gui.order.GuiGetServer;
import net.creeperhost.minetogether.gui.order.GuiServerInfo;
import net.creeperhost.minetogether.gui.serverlist.data.Invite;
import net.creeperhost.minetogether.gui.serverlist.data.Server;
import net.creeperhost.minetogether.gui.serverlist.data.ServerListNoEdit;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiInvited;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiMultiplayerPublic;
import net.creeperhost.minetogether.irc.IrcHandler;
import net.creeperhost.minetogether.mtconnect.FriendsServerList;
import net.creeperhost.minetogether.mtconnect.OurServerListEntryLanScan;
import net.creeperhost.minetogether.oauth.ServerAuthTest;
import net.creeperhost.minetogether.misc.Callbacks;
import net.creeperhost.minetogether.proxy.Client;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.serverstuffs.CreeperHostServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EventHandler
{
    private static final Logger logger = LogManager.getLogger();

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final int MAIN_BUTTON_ID = 30051988;
    private static final int MP_BUTTON_ID = 8008135;
    private static final int CHAT_BUTTON_ID = 800813;
    private static final int FRIEND_BUTTON_ID = 1337420;
    private static final int CHAT_BUTTON_ID_MAINMENU = 800813;
    private static final int FRIEND_BUTTON_ID_MAINMENU = 1337420;
    private static final int MINIGAMES_BUTTON_ID = 0xdeadbeef;
    
    private static GuiServerInfo guiServerInfo = new GuiServerInfo();
    private static Field reasonField = null;
    private static Field messageField = null;
    private static Field parentField = null;
    private static Field networkManagerField = null;
    private static NetworkManager lastNetworkManager = null;
    private static Field serverListSelectorField;
    private static Field serverListInternetField;
    private static Field lanServerListField;
    private static Field ServerListEntryLanScanField;
    private static int ticks = 0;
    private final ResourceLocation earlyResource = new ResourceLocation("textures/gui/achievement/achievement_background.png");
    private final ResourceLocation newResouce = new ResourceLocation("textures/gui/toasts.png");
    Field serverListField = null;
    Field editButtonField = null;
    Minecraft mc = Minecraft.getMinecraft();
    GuiScreen fakeGui = new GuiScreen() {};
    String mcVersion;
    int u = 0;
    int v = 0;
    private GuiMultiplayer lastInitialized = null;
    private ServerListNoEdit ourServerList;
    private boolean hasJoinedWorld;
    private CompletableFuture inviteCheckFuture;
    private int inviteTicks = -1;

    private int clientTicks = 0; //Tick counter client side.
    private UUID lastUUID; //Last UUID we saw the client using.
    public static boolean connectToChat = false; //If a connection is scheduled
    public static boolean disconnectFromChat = false; //If a disconnection is scheduled
    public static boolean chatDisconnected = false; //If we know we are disconnected.

    public static boolean isOnline = false;
    private static Future<?> onlineCheckFuture;
    
    public static NetworkManager getNetworkManager(GuiConnecting con)
    {
        long time = System.currentTimeMillis() + 5000;
        try
        {
            if (networkManagerField == null)
            {
                networkManagerField = ReflectionHelper.findField(con.getClass(), "networkManager", "field_146373_h", "");
                networkManagerField.setAccessible(true);
            }
            
            NetworkManager manager = null;
            while (manager == null) // loop to wait until networkManager is set.
            {
                if (System.currentTimeMillis() > time)
                    break;
                manager = (NetworkManager) networkManagerField.get(con);
            }
            
            return manager;
        } catch (Exception e)
        {
            return null;
        }
    }
    
    boolean first = true;
    
    Field defaultInputFieldTextField = null;

    boolean firstOpen = true;

    boolean firstConnect = true;
    
    @SubscribeEvent
    public void guiOpen(GuiOpenEvent event)
    {
        GuiScreen gui = event.getGui();
        GuiScreen curGui = Minecraft.getMinecraft().currentScreen;

        if(gui instanceof GuiMainMenu && first)
        {
            File offline = new File("local/minetogether/offline.txt");

            if(!isOnline && !offline.exists())
            {
                first = false;
                event.setGui(new GuiOffline());
            }
        }

        if(!isOnline) return;
        
        if (gui instanceof GuiMainMenu && (Config.getInstance().isServerListEnabled() || Config.getInstance().isChatEnabled())) {
            if (first) {
                first = false;
                CreeperHost.proxy.startChat();
            }
        }
        
        if (gui instanceof GuiDisconnected)
        {
            GuiDisconnected dc = (GuiDisconnected) gui;
            
            if (CreeperHost.instance.activeMinigame != null && CreeperHost.instance.joinTime + 30000 < System.currentTimeMillis())
            {
                Aries aries = new Aries("", "");
                Thread thread = new Thread(() ->
                {
                    Map<String, String> sendMap = new HashMap<>();
                    sendMap.put("id", String.valueOf(CreeperHost.instance.minigameID));
                    Map result = aries.doApiCall("minetogether", "failedminigame", sendMap);
                    CreeperHost.instance.getLogger().error("Failed to start minigame {} {}", CreeperHost.instance.minigameID, result);
                });
                thread.setName("Failed Thread");
                thread.setDaemon(false);
                thread.start();
            }
            
            try
            {
                if (reasonField == null)
                {
                    reasonField = ReflectionHelper.findField(gui.getClass(), "reason", "field_146306_a", "");
                    reasonField.setAccessible(true);
                }
                
                if (messageField == null)
                {
                    messageField = ReflectionHelper.findField(gui.getClass(), "message", "field_146304_f", "");
                    messageField.setAccessible(true);
                }
                
                if (parentField == null)
                {
                    parentField = ReflectionHelper.findField(gui.getClass(), "parentScreen", "field_146307_h", "");
                    parentField.setAccessible(true);
                }
                String reason = (String) reasonField.get(dc);
                ITextComponent message = (ITextComponent) messageField.get(dc);
                
                if (curGui instanceof GuiProgressDisconnected)
                {
                    if (message.getUnformattedText().contains("Server is still pre-generating!"))
                    {
                        GuiProgressDisconnected curDiscon = (GuiProgressDisconnected) curGui;
                        curDiscon.update(reason, message);
                        event.setCanceled(true);
                    }
                } else if (message.getUnformattedText().contains("Server is still pre-generating!"))
                {
                    event.setGui(new GuiProgressDisconnected((GuiScreen) parentField.get(dc), reason, message, lastNetworkManager));
                    lastNetworkManager = null;
                }
            } catch (Throwable ignored) {}
        } else if (gui instanceof GuiConnecting)
        {
            //lastNetworkManager = getNetworkManager((GuiConnecting) gui);
        } else if (gui instanceof GuiMultiplayer)
        {
            if (!CreeperHost.instance.trialMinigame && CreeperHost.instance.activeMinigame != null)
            {
                CreeperHost.instance.trialMinigame = true;
                event.setGui(new GuiMinigames(null, true));
            }
        } else if (gui instanceof GuiChat && Config.getInstance().isChatEnabled() && !CreeperHost.instance.ingameChat.hasDisabledIngameChat())
        {
            String presetString = "";
            boolean sleep = false;
            if (gui instanceof GuiSleepMP)
            {
                sleep = true;
            }
            
            if (defaultInputFieldTextField == null)
            {
                try
                {
                    defaultInputFieldTextField = ReflectionHelper.findField(GuiChat.class, "defaultInputFieldText", "field_146409_v", "");
                } catch (Exception e) { e.printStackTrace(); }
            }
            try
            {
                presetString = (String) defaultInputFieldTextField.get(gui);
                MinecraftServer minecraftServerInstance = FMLCommonHandler.instance().getMinecraftServerInstance();

                if (Config.getInstance().isAutoMT() && minecraftServerInstance != null && minecraftServerInstance.isSinglePlayer() && Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs && firstOpen)
                {
                    firstOpen = false;
                    ((GuiNewChatOurs)Minecraft.getMinecraft().ingameGUI.getChatGUI()).setBase(!CreeperHost.instance.gdpr.hasAcceptedGDPR());
                }
            } catch (IllegalAccessException ignored) {}
            try {
                event.setGui(new GuiChatOurs(presetString, sleep));
            }catch (Exception ignored){}
        }
    }
    
    private GuiButton ingameChatButton = null;
    
    @SuppressWarnings("Duplicates")
    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event)
    {
        boolean buttonDrawn = false;

        final GuiScreen gui = event.getGui();

        if (firstConnect && gui instanceof GuiMainMenu)
        {
            firstConnect = false;
            String server = System.getProperty("mt.server");
            int serverId = -1;
            if (server != null)
            {
                try {
                    serverId = Integer.parseInt(server);
                } catch (Throwable t) {
                    logger.error("Unable to auto connect to server as unable to parse server ID");
                }

                Server serverObj = Callbacks.getServer(serverId);



                if (serverObj != null)
                {
                    String[] serverSplit = serverObj.host.split(":");

                    int realPort = -1;
                    try {
                         realPort = Integer.parseInt(serverSplit[1]);
                    } catch (Throwable t) {
                        logger.error("Unable to auto connect to server as unable to parse server port for ID " + serverId);
                    }

                    if(realPort != -1) net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServerAtStartup(serverSplit[0], realPort);
                }
            }
        }

        if(!isOnline) return;

        if (Config.getInstance().isMainMenuEnabled() && gui instanceof GuiMainMenu)
        {
            CreeperHost.instance.setRandomImplementation();
            if (CreeperHost.instance.getImplementation() == null)
                return;
            List<GuiButton> buttonList = event.getButtonList();
            for(GuiButton b : buttonList)
            {
                if(b.id == 14 && Config.getInstance().getReplaceRealms())
                {
                    b.displayString = I18n.format("minetogether.realms.replace");
                    break;
                }
            }
            if (buttonList != null && !Config.getInstance().getReplaceRealms())
            {
                //Multiplayer button
                if(buttonList.size() > 2) {
                    int x = buttonList.get(2).xPosition - buttonList.get(2).width - 28;
                    buttonList.add(new GuiButtonCreeper(MAIN_BUTTON_ID, x, gui.height / 4 + 48 + 24));
                } else {
                    buttonList.add(new GuiButtonCreeper(MAIN_BUTTON_ID, gui.width / 2 + 104, gui.height / 4 + 48 + 72 + 12));
                }
            }

            buttonDrawn = true;

            if(Config.getInstance().isEnableMainMenuFriends())
            {
                event.getButtonList().add(new GuiButton(FRIEND_BUTTON_ID_MAINMENU, gui.width - 100 - 5, 5, 100, 20, I18n.format("creeperhost.multiplayer.friends")));
                event.getButtonList().add(new GuiButtonMultiple(CHAT_BUTTON_ID_MAINMENU, gui.width - 25 - 99, 5, 1));
            }

        } else if (gui instanceof GuiMultiplayer && !(gui instanceof GuiMultiplayerPublic) && lastInitialized != gui) {
            GuiMultiplayer mpGUI = (GuiMultiplayer) gui;
			if (CreeperHost.instance.getImplementation() == null)
				CreeperHost.instance.setRandomImplementation();
            if (Config.getInstance().isMpMenuEnabled() && CreeperHost.instance.getImplementation() != null)
            {
                try
                {
                    if (serverListSelectorField == null)
                    {
                        serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "serverListSelector", "field_146803_h", "");
                        serverListSelectorField.setAccessible(true);
                    }
                    
                    if (serverListInternetField == null)
                    {
                        serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "serverListInternet", "field_148198_l", "");
                        serverListInternetField.setAccessible(true);
                    }

                    if (lanServerListField == null)
                    {
                        lanServerListField = ReflectionHelper.findField(GuiMultiplayer.class, "lanServerList", "field_146799_A", "");
                        lanServerListField.setAccessible(true);
                    }

                    if(ServerListEntryLanScanField == null)
                    {
                        ServerListEntryLanScanField = ReflectionHelper.findField(ServerSelectionList.class, "lanScanEntry", "field_148196_n", "");
                        ServerListEntryLanScanField.setAccessible(true);
                    }


                    ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(mpGUI); // Get the old selector
                    List serverListInternet = (List) serverListInternetField.get(serverListSelector); // Get the list from inside it
                    CreeperHostServerSelectionList ourList = new CreeperHostServerSelectionList(mpGUI, Minecraft.getMinecraft(), mpGUI.width, mpGUI.height, 32, mpGUI.height - 64, 36);
                    ourList.replaceList(serverListInternet);
                    serverListInternetField.set(ourList, serverListInternet);
                    serverListSelectorField.set(mpGUI, ourList);

                    // friends stuff
                    LanServerDetector.LanServerList oldLanServerList = (LanServerDetector.LanServerList) lanServerListField.get(mpGUI); // get the old lan server list
                    lanServerListField.set(mpGUI, new FriendsServerList(oldLanServerList, mpGUI)); // we wrap it because there is a thread which works on the old stuff. Rather than doing more reflection this seemed ok
                    ServerListEntryLanScanField.set(ourList, new OurServerListEntryLanScan());//This was far too much work to replace a string
                } catch (Throwable e)
                {
                    CreeperHost.logger.warn("Reflection to alter server list failed.", e);
                }
            }

            if (Config.getInstance().isServerListEnabled())
            {
                try
                {
                    if (serverListField == null)
                    {
                        serverListField = ReflectionHelper.findField(GuiMultiplayer.class, "savedServerList", "field_146804_i", "");
                        serverListField.setAccessible(true);
                    }
                    
                    ourServerList = new ServerListNoEdit(Minecraft.getMinecraft());
                    serverListField.set(mpGUI, ourServerList);
                    ourServerList.loadServerList();
                    
                    
                    if (serverListSelectorField == null)
                    {
                        serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "serverListSelector", "field_146803_h", "");
                        serverListSelectorField.setAccessible(true);
                    }
                    
                    ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(mpGUI); // Get the old selector
                    serverListSelector.updateOnlineServers(ourServerList);
                    
                    
                } catch (IllegalAccessException ignored) {}
            }
            lastInitialized = mpGUI;
        }
        
        if (Config.getInstance().isServerListEnabled())
        {
            if (gui instanceof GuiMultiplayer && !(gui instanceof GuiMultiplayerPublic))
            {
                GuiButton mpbtn = new GuiButton(MP_BUTTON_ID, gui.width - 100 - 5, 5, 100, 20, I18n.format("creeperhost.multiplayer.public"));
                Config defaultConfig = new Config();
                if (defaultConfig.curseProjectID.equals(Config.getInstance().curseProjectID)) mpbtn.enabled = false;
                event.getButtonList().add(mpbtn);
                buttonDrawn = true;
                GuiButton editButton = null;
                for (int i = 0; i < event.getButtonList().size(); i++)
                {
                    GuiButton button = event.getButtonList().get(i);
                    if (button.id == 7)
                    {
                        editButton = button;
                        break;
                    }
                }
                
                if (editButton != null)
                {
                    event.getButtonList().remove(editButton);
                    ServerSelectionList list = null;
                    
                    if (serverListSelectorField == null)
                    {
                        serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "serverListSelector", "field_146803_h", "");
                        serverListSelectorField.setAccessible(true);
                    }
                    
                    try
                    {
                        list = (ServerSelectionList) serverListSelectorField.get(gui);
                    } catch (IllegalAccessException ignored) {}
                    
                    final ServerSelectionList finalList = list;
                    event.getButtonList().add(editButton = new GuiButton(7, gui.width / 2 - 154, gui.height - 28, 70, 20, I18n.format("selectServer.edit"))
                    {
                        
                        public void func_191745_a(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_)
                        {
                            myDrawButton(p_191745_1_, p_191745_2_, p_191745_3_);
                        }
                        
                        // < 1.12 compat
                        public void func_146112_a(Minecraft mc, int mouseX, int mouseY)
                        {
                            myDrawButton(mc, mouseX, mouseY);
                        }
                        
                        public void myDrawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_)
                        {
                            int placeInList = finalList.getSelected();
                            GuiListExtended.IGuiListEntry iguilistentry = placeInList < 0 ? null : finalList.getListEntry(placeInList);
                            if (iguilistentry instanceof ServerListEntryNormal)
                            {
                                if (ourServerList.isLocked(placeInList))
                                {
                                    enabled = false;
                                    if (hovered)
                                    {
                                        ((GuiMultiplayer) gui).setHoveringText("Cannot edit as was added from public server list!");
                                    }
                                } else
                                {
                                    enabled = true;
                                }
                            }
                            
                            // Below copied from GuiButton code to avoid having to use reflection logic to call the right function
                            
                            if (this.visible)
                            {
                                FontRenderer fontrenderer = p_146112_1_.fontRendererObj;
                                p_146112_1_.getTextureManager().bindTexture(BUTTON_TEXTURES);
                                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                this.hovered = p_146112_2_ >= this.xPosition && p_146112_3_ >= this.yPosition && p_146112_2_ < this.xPosition + this.width && p_146112_3_ < this.yPosition + this.height;
                                int i = this.getHoverState(this.hovered);
                                GlStateManager.enableBlend();
                                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                                this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
                                this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                                this.mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
                                int j = 14737632;
                                
                                if (packedFGColour != 0)
                                {
                                    j = packedFGColour;
                                } else if (!this.enabled)
                                {
                                    j = 10526880;
                                } else if (this.hovered)
                                {
                                    j = 16777120;
                                }
                                
                                this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
                            }
                        }
                    });
                    
                    editButton.enabled = false;
                    
                    if (editButtonField == null)
                    {
                        editButtonField = ReflectionHelper.findField(GuiMultiplayer.class, "btnEditServer", "field_146810_r", "");
                        editButtonField.setAccessible(true);
                    }
                    try
                    {
                        editButtonField.set(gui, editButton);
                    } catch (IllegalAccessException ignored) {}
                }
            }
            
            if (gui instanceof GuiIngameMenu)
            {
                buttonDrawn = true;
                event.getButtonList().add(new GuiButton(FRIEND_BUTTON_ID, gui.width - 100 - 5, 5, 100, 20, I18n.format("creeperhost.multiplayer.friends")));
                int x = gui.width - 20 - 5;
                if (buttonDrawn) x -= 99;

                event.getButtonList().add(new GuiButtonMultiple(CHAT_BUTTON_ID, x, 5, 1));
            }

            if (gui instanceof GuiMainMenu)
            {
                
                int yPosition = 0;
                for (GuiButton butt : event.getButtonList())
                {
                    if (butt.id == 2)
                    {  // Multi Player
                        yPosition = butt.yPosition;
                        butt.width = 98;
                    }
                }
                
                event.getButtonList().add(new GuiButton(MINIGAMES_BUTTON_ID, gui.width / 2 + 2, yPosition, 98, 20, "Minigames"));
            }
            
            if (gui instanceof GuiMultiplayer && !(gui instanceof GuiMultiplayerPublic))
            {
                List<GuiButton> buttonList = event.getButtonList();
                for (GuiButton button : buttonList)
                {
                    if (button.id == 8)
                    {
                        button.visible = false;
                        button.enabled = false;
                    }
                    
                    if (button.id == 2)
                    {
                        button.xPosition -= 7;
                        button.width += 1;
                    }
                    
                    if (button.id == 4)
                    {
                        button.xPosition = gui.width / 2 - 8;
                        button.yPosition = gui.height - 28;
                        button.width -= 14;
                    }
                    
                    if (button.id == 3)
                    {
                        button.xPosition -= 25;
                    }
                    
                    
                    if (button.id == 0)
                    {
                        button.xPosition += 1;
                        button.width -= 2;
                    }
                }
                buttonList.add(new GuiButtonMultiple(8, gui.width / 2 + 133, gui.height - 52, 2));
                buttonList.add(new GuiButton(MINIGAMES_BUTTON_ID, gui.width / 2 - 50, gui.height - 52, 75, 20, "Minigames"));
            }
        }

        if (gui instanceof GuiMultiplayer && !(gui instanceof GuiMultiplayerPublic))
        {
            int x = gui.width - 20 - 5;
            if (buttonDrawn)
                x -= 99;

            boolean chatEnabled = Config.getInstance().isChatEnabled();

            event.getButtonList().add(new GuiButtonMultiple(CHAT_BUTTON_ID, x, 5, chatEnabled ? 1 : 3));
        }
        
        if (Config.getInstance().isChatEnabled())
        {
            if (gui instanceof ScreenChatOptions)
            {
                int i = 11;
                event.getButtonList().add(ingameChatButton = new GuiButton(-20, gui.width / 2 - 155 + i % 2 * 160, gui.height / 6 + 24 * (i >> 1), 150, 20, "MineTogether Chat: " + (CreeperHost.instance.ingameChat.hasDisabledIngameChat() ? "OFF" : "ON")));
            }
        }
    }
    
    @SubscribeEvent
    public void serverLoginEvent(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        if (!isOnline) return;
        hasJoinedWorld = false;
    }
    
    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if (!isOnline) return;
        if (!Config.getInstance().isSivIntegration())
            return;
        if (event.getWorld().isRemote && !hasJoinedWorld && Minecraft.getMinecraft().player != null)
        {
            hasJoinedWorld = true;
            CreeperHost.instance.makeQueryGetter();
            if (CreeperHost.instance.getQueryGetter() != null)
            {
                CreeperHost.instance.getQueryGetter().run();
            }
        }
    }
    boolean doubleCancel = false;
    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event)
    {
        if (!isOnline) return;
        GuiScreen gui = event.getGui();
        GuiButton button = event.getButton();
        if (gui instanceof GuiMainMenu)
        {
            if (button != null && button.id == MAIN_BUTTON_ID)
            {
                Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
            if (button != null && button.id == MINIGAMES_BUTTON_ID)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMinigames(gui));
            }
            if(button != null && button.id == 14 && Config.getInstance().getReplaceRealms())
            {
                Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
                event.setCanceled(true);
            }

            if (button != null && button.id == FRIEND_BUTTON_ID_MAINMENU)
            {
                CreeperHost.proxy.openFriendsGui();
            }

            if (button != null && button.id == CHAT_BUTTON_ID_MAINMENU)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMTChat(gui));
            }
        } else if (gui instanceof GuiMultiplayer)
        {
            if (button != null && button.id == MP_BUTTON_ID)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(gui));
            }
            if (button != null && button.id == CHAT_BUTTON_ID)
            {
                if(Config.getInstance().isChatEnabled()) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiMTChat(gui));
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiSettings(gui));
                }
            }
            if (button != null && button.id == MINIGAMES_BUTTON_ID)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMinigames(gui));
            }
            if(button != null && button.id == 0) {
                //TODO: Please give me a better way to fix this, reading the existing code to figure out why the cancel button doesn't work properly will give me a anxiety.
                if(gui instanceof GuiMultiplayerPublic)
                {
                    doubleCancel = true;
                } else {
                    if(doubleCancel)
                    {
                        doubleCancel = false;
                        event.setCanceled(true);
                        Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
                    }
                }
            }
        } else if (gui instanceof GuiIngameMenu)
        {
            if (button != null && button.id == FRIEND_BUTTON_ID)
            {
                CreeperHost.proxy.openFriendsGui();
            }
            
            if (button != null && button.id == CHAT_BUTTON_ID)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMTChat(gui));
            }
        } else if (gui instanceof ScreenChatOptions)
        {
            if (button == ingameChatButton)
            {
                boolean chatEnabled = !CreeperHost.instance.ingameChat.hasDisabledIngameChat();
                if (chatEnabled)
                    CreeperHost.proxy.disableIngameChat();
                else
                    CreeperHost.proxy.enableIngameChat();
                
                button.displayString = "MineTogether Chat: " + (chatEnabled ? "OFF" : "ON");
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (!isOnline) return;
        if (!Config.getInstance().isSivIntegration())
        {
            return;
        }
        if (event.getType() != RenderGameOverlayEvent.ElementType.PLAYER_LIST)
        {
            return;
        }
        if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped())
        {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        
        ScaledResolution resolution = new ScaledResolution(mc);
        guiServerInfo.setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
        if (guiServerInfo.renderServerInfo())
        {
            event.setCanceled(true);
        }
    }
    
    private boolean updateNeeded = false;
    
    private Thread updateThread = null;
    
    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
        if (!isOnline) return;
        ServerAuthTest.processPackets();
        if (!Config.getInstance().isSivIntegration())
            return;
        guiServerInfo.doTick();
        if (!((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown()) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped())
        {
            return;
        }
        
        if (updateThread == null)
        {
            updateThread = new Thread(() ->
            {
                while (true)
                {
                    if (updateNeeded)
                    {
                        updateNeeded = false;
                        if (CreeperHost.instance.getQueryGetter() != null)
                        {
                            CreeperHost.instance.getQueryGetter().run();
                        }
                    }
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }
            });
            
            updateThread.setDaemon(true);
            updateThread.setName("SIP Update Thread");
            updateThread.start();
        }
        
        try
        {
            if (ticks == 0)
            {
                ticks = 40;
                updateNeeded = true;
                
            }
            ticks--;
        } catch (Throwable ignored) {}
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        if (!CreeperHost.MOD_ID.equals(eventArgs.getModID()))
        {
            return;
        }
        
        CreeperHost.instance.saveConfig(false);
    }
    
    @SubscribeEvent
    public void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent evt)
    {
        CreeperHost.instance.curServerId = -1;
        CreeperHostServer.serverOn = false;
        CreeperHostServer.updateID = -1;
        CreeperHostServer.secret = null;
    }
    
    @SuppressWarnings("Duplicates")
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent evt)
    {
        if (evt.phase == TickEvent.Phase.START)
        {
            if (clientTicks % (10 * 20) == 0 && CreeperHost.instance.gdpr.hasAcceptedGDPR() && Config.getInstance().isChatEnabled()) { //Every second is bad, Bad bad Covers
                CreeperHost.proxy.reCacheUUID(); //Careful with this, recomputes the GameProfile
                UUID currentUUID = CreeperHost.proxy.getUUID();
                if (lastUUID == null)
                {
                    lastUUID = currentUUID;
                }
                if (!lastUUID.equals(currentUUID))
                {
                    if (onlineCheckFuture == null || onlineCheckFuture.isDone())
                    {
                        onlineCheckFuture = executor.submit(() -> {
                            isOnline = CreeperHost.proxy.checkOnline();
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
                IrcHandler.reconnect();
                chatDisconnected = true;
            }
            connectToChat = false;
            disconnectFromChat = false;
        }
        if (evt.phase == TickEvent.Phase.END) {
            clientTicks++;
        }
        if (!isOnline) return;

        //CreeperHost.instance.curServerId = CreeperHostServer.updateID;
        inviteTicks = (inviteTicks + 1) % 300;
        if (inviteTicks != 0)
            return;
        
        if (Config.getInstance().isServerListEnabled() && CreeperHost.instance.gdpr.hasAcceptedGDPR())
        {
            if (!(inviteCheckFuture != null && !inviteCheckFuture.isDone()))
            {
                inviteCheckFuture = CompletableFuture.runAsync(() -> {
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

                                synchronized (CreeperHost.instance.inviteLock)
                                {
                                    if (tempInvite != null)
                                        CreeperHost.instance.invite = tempInvite;
                                }

                                if (temp != null)
                                {
                                    CreeperHost.instance.displayToast(I18n.format("Your friend %s invited you to a private chat", CreeperHost.instance.getNameForUser(temp.getOwner()), ((Client) CreeperHost.proxy).openGuiKey.getDisplayName()), 5000, () ->
                                    {
                                        mc.displayGuiScreen(new GuiMTChat(Minecraft.getMinecraft().currentScreen, true));
                                    });
                                }
                            }
                        }
                        catch (Exception ignored) {}

                        try
                        {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {}
                    }
                }, CreeperHost.otherExecutor);
            }
            
            boolean handled = false;
            synchronized (CreeperHost.instance.inviteLock)
            {
                if (CreeperHost.instance.invite != null)
                {
                    CreeperHost.instance.handledInvite = CreeperHost.instance.invite;
                    CreeperHost.instance.invite = null;
                    
                    handled = true;
                }
            }
            
            if (handled)
            {
                ArrayList<Friend> friendsList = Callbacks.getFriendsList(true);
                String friendName = "Unknown";
                
                for (Friend friend : friendsList)
                {
                    if (friend.getCode().equals(CreeperHost.instance.handledInvite.by))
                    {
                        friendName = friend.getName();
                        CreeperHost.instance.handledInvite.by = friendName;
                        break;
                    }
                }
                if (mc.currentScreen != null && mc.currentScreen instanceof GuiFriendsList)
                {
                    CreeperHost.proxy.openFriendsGui();
                } else
                {
                    CreeperHost.instance.displayToast(I18n.format("creeperhost.multiplayer.invitetoast", ((Client) CreeperHost.proxy).openGuiKey.getDisplayName()), 10000, ()->
                    {
                        mc.displayGuiScreen(new GuiInvited(CreeperHost.instance.handledInvite, mc.currentScreen));
                        CreeperHost.instance.handledInvite = null;
                    });
                }
            }
        }
        
        if (Config.getInstance().isChatEnabled())
        {
            String friend;
            boolean friendMessage;
            
            synchronized (CreeperHost.instance.friendLock)
            {
                friend = CreeperHost.instance.friend;
                friendMessage = CreeperHost.instance.friendMessage;
                CreeperHost.instance.friend = null;
            }
            
            if (friend != null)
            {
                if (friendMessage && Minecraft.getMinecraft().currentScreen instanceof GuiMTChat)
                    return;
                if(Config.getInstance().isFriendOnlineToastsEnabled())
                {
                    CreeperHost.instance.displayToast(I18n.format(friendMessage ? "%s has sent you a message!" : "Your friend %s has come online!", friend), 2000, null);
                }
            }
        }
    }
    
    private ResourceLocation getToastResourceLocation()
    {
        if (mcVersion == null)
            try
            {
                /*
                We need to get this at runtime as Java is smart and interns final fields.
                Certainly not the dirtiest hack we do in this codebase.
                */
                mcVersion = (String) ForgeVersion.class.getField("mcVersion").get(null);
            } catch (Throwable e)
            {
                mcVersion = "unknown"; // will default to new method
            }
        String[] split = mcVersion.split("\\.");
        if (split.length >= 2)
        {
            if (split[1].equals("10") || split[1].equals("11") || split[1].equals("9") || split[1].equals("7"))
            {
                u = 96;
                v = 202;
                return earlyResource;
            }
        }
        return newResouce;
    }
    
    @SubscribeEvent
    public void guiRendered(TickEvent.RenderTickEvent evt)
    {
        if (!isOnline) return;
        if (!Config.getInstance().isFriendOnlineToastsEnabled()) return;

        if (CreeperHost.instance.toastText != null)
        {
            long curTime = System.currentTimeMillis();
            if (CreeperHost.instance.fadeTime > curTime)
            {
                long fadeDiff = CreeperHost.instance.fadeTime - CreeperHost.instance.endTime;
                long curFade = Math.min(CreeperHost.instance.fadeTime - curTime, fadeDiff);
                float alpha = (float) curFade / (float) fadeDiff;
                
                RenderHelper.disableStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
                mc.renderEngine.bindTexture(getToastResourceLocation());
                ScaledResolution res = new ScaledResolution(mc);
                drawTexturedModalRect(res.getScaledWidth() - 160, 0, u, v, 160, 32);
                GlStateManager.enableBlend();
                int textColour = (0xFFFFFF << 32) | ((int) (alpha * 255) << 24);
                mc.fontRendererObj.drawSplitString(CreeperHost.instance.toastText, res.getScaledWidth() - 160 + 5, 3, 160, textColour);
            } else
            {
                CreeperHost.instance.clearToast(false);
            }
        }
    }

    public void handleToastInteraction()
    {
        Runnable method = CreeperHost.instance.toastMethod;
        CreeperHost.instance.clearToast(false);
        if (method != null) method.run();
    }

    @SubscribeEvent
    public void onKeyboardInputGui(GuiScreenEvent.KeyboardInputEvent.Pre event)
    {
        if (!isOnline) return;
        onKeyInputGeneric();
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (!isOnline) return;
        onKeyInputGeneric();
    }

    private void onKeyInputGeneric()
    {
        KeyBinding binding = ((Client) CreeperHost.proxy).openGuiKey;
        boolean pressed = binding.getKeyCode() != 0 && Keyboard.isKeyDown(binding.getKeyCode()) && binding.getKeyConflictContext().isActive() && binding.getKeyModifier().isActive(binding.getKeyConflictContext());
        if (!pressed)
            return;
        if (CreeperHost.instance.isActiveToast())
        {
            handleToastInteraction();
        } else if (Config.getInstance().isServerListEnabled())
        {
            CreeperHost.proxy.openFriendsGui();
        }
    }


    @SubscribeEvent
    public void onMouseInputEvent(GuiScreenEvent.MouseInputEvent.Pre event) // this fires on mouse clicked in any GUI, and allows us to cancel it
    {
        if (!isOnline) return;
        MouseEvent mouseEvent = new MouseEvent(); // convenient shortcut to get everything we need
        event.setCanceled(onMouseInput(mouseEvent));
    }

    public boolean onMouseInput(MouseEvent event)
    {
        GuiScreen activeScreen = Minecraft.getMinecraft().currentScreen;
        if (activeScreen == null)
            return false; // just to stop the compiler from bitching mainly, and better to be safe than sorry
        if (!(event.isButtonstate() && event.getButton() == 0))
            return false;
        if (!CreeperHost.instance.isActiveToast())
            return false;
        int x = event.getX() * activeScreen.width / this.mc.displayWidth;
        int y = activeScreen.height - event.getY() * activeScreen.height / this.mc.displayHeight - 1;
        if (x > activeScreen.width - 160 && y < 32)
        {
            handleToastInteraction();
            return true;
        }
        return false;
    }

    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        fakeGui.drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }
}
