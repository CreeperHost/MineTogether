package net.creeperhost.minetogether;

import net.creeperhost.minetogether.aries.Aries;
import net.creeperhost.minetogether.client.gui.GuiProgressDisconnected;
import net.creeperhost.minetogether.client.gui.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.client.gui.element.GuiButtonCreeper;
import net.creeperhost.minetogether.client.gui.mpreplacement.CreeperHostServerSelectionList;
import net.creeperhost.minetogether.client.gui.serverlist.data.ServerListNoEdit;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler
{
    private static final int MAIN_BUTTON_ID = 30051988;
    private static final int MP_BUTTON_ID = 8008135;
    private static final int CHAT_BUTTON_ID = 800813;
    private static final int FRIEND_BUTTON_ID = 1337420;
    private static final int MINIGAMES_BUTTON_ID = 0xdeadbeef;
    
    //    private static GuiServerInfo guiServerInfo = new GuiServerInfo();
    private static Field reasonField = null;
    private static Field messageField = null;
    private static Field parentField = null;
    private static Field networkManagerField = null;
    private static NetworkManager lastNetworkManager = null;
    private static Field serverListSelectorField;
    private static Field serverListInternetField;
    private static int ticks = 0;
    Field serverListField = null;
    Field editButtonField = null;
    Minecraft mc = Minecraft.getInstance();
    Screen fakeGui = new Screen(new StringTextComponent(""))
    {
    };
    private MultiplayerScreen lastInitialized = null;
    private ServerListNoEdit ourServerList;
    private boolean hasJoinedWorld;
    
    public static NetworkManager getNetworkManager(ConnectingScreen con)
    {
        long time = System.currentTimeMillis() + 5000;
        try
        {
//            if (networkManagerField == null)
//            {
//                networkManagerField = ReflectionHelper.findField(con.getClass(), "networkManager", "field_146373_h", "");
//                networkManagerField.setAccessible(true);
//            }
            
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
    
    @SubscribeEvent
    public void guiOpen(GuiOpenEvent event)
    {
        Screen gui = event.getGui();
        Screen curGui = Minecraft.getInstance().currentScreen;
        
        if (gui instanceof DisconnectedScreen)
        {
            DisconnectedScreen dc = (DisconnectedScreen) gui;
            
            if (MineTogether.instance.activeMinigame != null && MineTogether.instance.joinTime + 30000 < System.currentTimeMillis())
            {
                Aries aries = new Aries("", "");
                Thread thread = new Thread(() ->
                {
                    Map<String, String> sendMap = new HashMap<>();
                    sendMap.put("id", String.valueOf(MineTogether.instance.minigameID));
                    Map result = aries.doApiCall("minetogether", "failedminigame", sendMap);
                    MineTogether.instance.getLogger().error("Failed to start minigame {} {}", MineTogether.instance.minigameID, result);
                });
                thread.setName("Failed Thread");
                thread.setDaemon(false);
                thread.start();
            }
            
            try
            {
//                if (reasonField == null)
//                {
//                    reasonField = ReflectionHelper.findField(gui.getClass(), "reason", "field_146306_a", "");
//                    reasonField.setAccessible(true);
//                }
//
//                if (messageField == null)
//                {
//                    messageField = ReflectionHelper.findField(gui.getClass(), "message", "field_146304_f", "");
//                    messageField.setAccessible(true);
//                }
//
//                if (parentField == null)
//                {
//                    parentField = ReflectionHelper.findField(gui.getClass(), "parentScreen", "field_146307_h", "");
//                    parentField.setAccessible(true);
//                }
                String reason = (String) reasonField.get(dc);
                ITextComponent message = (ITextComponent) messageField.get(dc);
                
                if (curGui instanceof GuiProgressDisconnected)
                {
                    if (message.getUnformattedComponentText().contains("Server is still pre-generating!"))
                    {
                        GuiProgressDisconnected curDiscon = (GuiProgressDisconnected) curGui;
                        curDiscon.update(reason, message);
                        event.setCanceled(true);
                    }
                } else if (message.getUnformattedComponentText().contains("Server is still pre-generating!"))
                {
                    event.setGui(new GuiProgressDisconnected((ConnectingScreen) parentField.get(dc), reason, message, lastNetworkManager));
                    lastNetworkManager = null;
                }
            } catch (Throwable ignored)
            {
            }
        } else if (gui instanceof ConnectingScreen)
        {
            //lastNetworkManager = getNetworkManager((GuiConnecting) gui);
        } else if (gui instanceof MultiplayerScreen)
        {
            if (!MineTogether.instance.trialMinigame && MineTogether.instance.activeMinigame != null)
            {
                MineTogether.instance.trialMinigame = true;
//                event.setGui(new GuiMinigames(null, true));
            }
        } else if (gui instanceof ChatScreen && Config.getInstance().isChatEnabled() && !MineTogether.instance.ingameChat.hasDisabledIngameChat())
        {
            String presetString = "";
            boolean sleep = false;
            if (gui instanceof SleepInMultiplayerScreen)
            {
                sleep = true;
            }

//            if (defaultInputFieldTextField == null)
//            {
//                try
//                {
//                    defaultInputFieldTextField = ReflectionHelper.findField(GuiChat.class, "defaultInputFieldText", "field_146409_v", "");
//                } catch (Exception e) { e.printStackTrace(); }
//            }
            try
            {
                presetString = (String) defaultInputFieldTextField.get(gui);
//                MinecraftServer minecraftServerInstance = FMLCommonHandler.instance().getMinecraftServerInstance();
//
//                if (Config.getInstance().isAutoMT() && minecraftServerInstance != null && minecraftServerInstance.isSinglePlayer() && Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs && firstOpen)
//                {
//                    firstOpen = false;
//                    ((GuiNewChatOurs)Minecraft.getInstance().ingameGUI.getChatGUI()).setBase(!MineTogether.instance.gdpr.hasAcceptedGDPR());
//                }
            } catch (IllegalAccessException ignored)
            {
            }
            try
            {
                event.setGui(new GuiChatOurs(presetString, sleep));
            } catch (Exception ignored)
            {
            }
        }
    }
    
    private Button ingameChatButton = null;
    
    @SuppressWarnings("Duplicates")
    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event)
    {
        boolean buttonDrawn = false;
        
        final Screen gui = event.getGui();
        if (Config.getInstance().isMainMenuEnabled() && gui instanceof MainMenuScreen)
        {
            MineTogether.instance.setRandomImplementation();
            if (MineTogether.instance.getImplementation() == null)
                return;
            List<Button> buttonList = null;
            if (buttonList != null)
            {
                //Multiplayer button
                if (buttonList.size() > 2)
                {
                    //if(buttonList.contains(2)) {
                    int x = buttonList.get(2).x - buttonList.get(2).getWidth() - 26;
                    buttonList.add(new GuiButtonCreeper(x, gui.height / 4 + 48 + 24, p ->
                    {
                    
                    }));
                } else
                {
                    buttonList.add(new GuiButtonCreeper(gui.width / 2 + 104, gui.height / 4 + 48 + 72 + 12, p ->
                    {
                    
                    }));
                }
            }
        } else if (gui instanceof MultiplayerScreen) //&& !(gui instanceof GuiMultiplayerPublic) && lastInitialized != gui)
        {
            MultiplayerScreen mpGUI = (MultiplayerScreen) gui;
            if (MineTogether.instance.getImplementation() == null)
                MineTogether.instance.setRandomImplementation();
            if (Config.getInstance().isMpMenuEnabled() && MineTogether.instance.getImplementation() != null)
            {
                try
                {
//                    if (serverListSelectorField == null)
//                    {
//                        serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "serverListSelector", "field_146803_h", "");
//                        serverListSelectorField.setAccessible(true);
//                    }

//                    if (serverListInternetField == null)
//                    {
//                        serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "serverListInternet", "field_148198_l", "");
//                        serverListInternetField.setAccessible(true);
//                    }
                    
                    ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(mpGUI); // Get the old selector
                    List serverListInternet = (List) serverListInternetField.get(serverListSelector); // Get the list from inside it
                    CreeperHostServerSelectionList ourList = new CreeperHostServerSelectionList(mpGUI, Minecraft.getInstance(), mpGUI.width, mpGUI.height, 32, mpGUI.height - 64, 36);
                    ourList.replaceList(serverListInternet);
                    serverListInternetField.set(ourList, serverListInternet);
                    serverListSelectorField.set(mpGUI, ourList);
                } catch (Throwable e)
                {
                    MineTogether.logger.warn("Reflection to alter server list failed.", e);
                }
            }
            
            if (Config.getInstance().isServerListEnabled())
            {
                try
                {
                    if (serverListField == null)
                    {
//                        serverListField = ReflectionHelper.findField(MultiplayerScreen.class, "savedServerList", "field_146804_i", "");
//                        serverListField.setAccessible(true);
                    }
                    
                    ourServerList = new ServerListNoEdit(Minecraft.getInstance());
                    serverListField.set(mpGUI, ourServerList);
                    ourServerList.loadServerList();
                    
                    
                    if (serverListSelectorField == null)
                    {
//                        serverListSelectorField = ReflectionHelper.findField(MultiplayerScreen.class, "serverListSelector", "field_146803_h", "");
//                        serverListSelectorField.setAccessible(true);
                    }
                    
                    ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(mpGUI); // Get the old selector
                    serverListSelector.updateOnlineServers(ourServerList);
                    
                    
                } catch (IllegalAccessException ignored)
                {
                }
            }
            lastInitialized = mpGUI;
        }
        
        if (Config.getInstance().isServerListEnabled())
        {
            if (gui instanceof MultiplayerScreen) //&& !(gui instanceof GuiMultiplayerPublic))
            {
                event.addWidget(new Button(gui.width - 100 - 5, 5, 100, 20, I18n.format("creeperhost.multiplayer.public"), p ->
                {
                
                }));
                buttonDrawn = true;
                Button editButton = null;
                for (int i = 0; i < event.getWidgetList().size(); i++)
                {
//                    Button button = event.getWidgetList().get(i);
//                    if (button.id == 7)
//                    {
//                        editButton = button;
//                        break;
//                    }
                }
                
                if (editButton != null)
                {
                    event.removeWidget(editButton);
                    ServerSelectionList list = null;

//                    if (serverListSelectorField == null)
//                    {
//                        serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "serverListSelector", "field_146803_h", "");
//                        serverListSelectorField.setAccessible(true);
//                    }
                    
                    try
                    {
                        list = (ServerSelectionList) serverListSelectorField.get(gui);
                    } catch (IllegalAccessException ignored)
                    {
                    }
                    
                    final ServerSelectionList finalList = list;
                    event.addWidget(editButton = new Button(gui.width / 2 - 154, gui.height - 28, 70, 20, "selectServer.edit", p ->
                    {
                    
                    }));
//                    {
//                        public void func_191745_a(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_)
//                        {
//                            myDrawButton(p_191745_1_, p_191745_2_, p_191745_3_);
//                        }
                    
                    // < 1.12 compat
//                        public void func_146112_a(Minecraft mc, int mouseX, int mouseY)
//                        {
//                            myDrawButton(mc, mouseX, mouseY);
//                        }
//
//                        public void myDrawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_)
//                        {
//                            int placeInList = finalList.getSelected();
//                            GuiListExtended.IGuiListEntry iguilistentry = placeInList < 0 ? null : finalList.getListEntry(placeInList);
//                            if (iguilistentry instanceof ServerListEntryNormal)
//                            {
//                                if (ourServerList.isLocked(placeInList))
//                                {
//                                    enabled = false;
//                                    if (hovered)
//                                    {
//                                        ((GuiMultiplayer) gui).setHoveringText("Cannot edit as was added from public server list!");
//                                    }
//                                } else
//                                {
//                                    enabled = true;
//                                }
//                            }
                    
                    // Below copied from GuiButton code to avoid having to use reflection logic to call the right function

//                            if (this.visible)
//                            {
//                                FontRenderer fontrenderer = p_146112_1_.fontRendererObj;
//                                p_146112_1_.getTextureManager().bindTexture(BUTTON_TEXTURES);
//                                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                                this.hovered = p_146112_2_ >= this.xPosition && p_146112_3_ >= this.yPosition && p_146112_2_ < this.xPosition + this.width && p_146112_3_ < this.yPosition + this.height;
//                                int i = this.getHoverState(this.hovered);
//                                GlStateManager.enableBlend();
//                                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//                                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//                                this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
//                                this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
//                                this.mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
//                                int j = 14737632;
//
//                                if (packedFGColour != 0)
//                                {
//                                    j = packedFGColour;
//                                } else if (!this.enabled)
//                                {
//                                    j = 10526880;
//                                } else if (this.hovered)
//                                {
//                                    j = 16777120;
//                                }
//
//                                this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
//                            }
//                        }
//                    });
//
//                    editButton.enabled = false;
//
//                    if (editButtonField == null)
//                    {
//                        editButtonField = ReflectionHelper.findField(GuiMultiplayer.class, "btnEditServer", "field_146810_r", "");
//                        editButtonField.setAccessible(true);
//                    }
//                    try
//                    {
//                        editButtonField.set(gui, editButton);
//                    } catch (IllegalAccessException ignored) {}
//                }
                }

//            if (gui instanceof GuiIngameMenu)
//            {
//                buttonDrawn = true;
//                event.getButtonList().add(new GuiButton(FRIEND_BUTTON_ID, gui.width - 100 - 5, 5, 100, 20, I18n.format("creeperhost.multiplayer.friends")));
//            }

//            if (gui instanceof MainMenuScreen)
//            {
//
//                int yPosition = 0;
//                for (GuiButton butt : event.getButtonList())
//                {
//                    if (butt.id == 2)
//                    {  // Multi Player
//                        yPosition = butt.yPosition;
//                        butt.width = 98;
//                    }
//                }
//
//                event.getButtonList().add(new GuiButton(MINIGAMES_BUTTON_ID, gui.width / 2 + 2, yPosition, 98, 20, "Minigames"));
            }

//            if (gui instanceof MultiplayerScreen && !(gui instanceof GuiMultiplayerPublic))
//            {
//                List<GuiButton> buttonList = event.getButtonList();
//                for (GuiButton button : buttonList)
//                {
//                    if (button.id == 8)
//                    {
//                        button.visible = false;
//                        button.enabled = false;
//                    }
//
//                    if (button.id == 2)
//                    {
//                        button.xPosition -= 7;
//                        button.width += 1;
//                    }
//
//                    if (button.id == 4)
//                    {
//                        button.xPosition = gui.width / 2 - 8;
//                        button.yPosition = gui.height - 28;
//                        button.width -= 14;
//                    }
//
//                    if (button.id == 3)
//                    {
//                        button.xPosition -= 25;
//                    }
//
//
//                    if (button.id == 0)
//                    {
//                        button.xPosition += 1;
//                        button.width -= 2;
//                    }
//                }
//                buttonList.add(new GuiButtonMultiple(8, gui.width / 2 + 133, gui.height - 52, 2));
//                buttonList.add(new GuiButton(MINIGAMES_BUTTON_ID, gui.width / 2 - 50, gui.height - 52, 75, 20, "Minigames"));
//            }
        }
        
        if (Config.getInstance().isChatEnabled())
        {
//            if (gui instanceof ScreenChatOptions)
//            {
//                int i = 11;
//                event.getButtonList().add(ingameChatButton = new GuiButton(-20, gui.width / 2 - 155 + i % 2 * 160, gui.height / 6 + 24 * (i >> 1), 150, 20, "MineTogether Chat: " + (MineTogether.instance.ingameChat.hasDisabledIngameChat() ? "OFF" : "ON")));
//            }

//            if (gui instanceof MultiplayerScreen && !(gui instanceof GuiMultiplayerPublic))
//            {
//                int x = gui.width - 20 - 5;
//                if (buttonDrawn)
//                    x -= 99;
////                event.getButtonList().add(new GuiButtonMultiple(CHAT_BUTTON_ID, x, 5, 1));
//            }
            
            if (gui instanceof IngameMenuScreen)
            {
                int x = gui.width - 20 - 5;
                if (buttonDrawn)
                    x -= 99;
//                event.getButtonList().add(new GuiButtonMultiple(CHAT_BUTTON_ID, x, 5, 1));
            }
        }
    }

//    @SubscribeEvent
//    public void serverLoginEvent(FMLNetworkEvent.ClientConnectedToServerEvent event)
//    {
//        hasJoinedWorld = false;
//    }

//    @SubscribeEvent
//    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
//    {
//        if (!Config.getInstance().isSivIntegration())
//            return;
//        if (event.getWorld().isRemote && !hasJoinedWorld && Minecraft.getInstance().player != null)
//        {
//            hasJoinedWorld = true;
//            MineTogether.instance.makeQueryGetter();
//            if (MineTogether.instance.getQueryGetter() != null)
//            {
//                MineTogether.instance.getQueryGetter().run();
//            }
//        }
//    }

//    @SubscribeEvent
//    public void onActionPerformed(ActionPerformedEvent.Pre event)
//    {
//        Screen gui = event.getGui();
//        Button button = event.getButton();
//        if (gui instanceof MainMenuScreen)
//        {
//            if (button != null && button.id == MAIN_BUTTON_ID)
//            {
//                Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
//            }
//            if (button != null && button.id == MINIGAMES_BUTTON_ID)
//            {
//                Minecraft.getMinecraft().displayGuiScreen(new GuiMinigames(gui));
//            }
//        } else if (gui instanceof GuiMultiplayer)
//        {
//            if (button != null && button.id == MP_BUTTON_ID)
//            {
//                Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(gui));
//            }
//            if (button != null && button.id == CHAT_BUTTON_ID)
//            {
//                Minecraft.getMinecraft().displayGuiScreen(new GuiMTChat(gui));
//            }
//            if (button != null && button.id == MINIGAMES_BUTTON_ID)
//            {
//                Minecraft.getMinecraft().displayGuiScreen(new GuiMinigames(gui));
//            }
//        } else if (gui instanceof IngameMenuScreen)
//        {
//            if (button != null && button.id == FRIEND_BUTTON_ID)
//            {
//                MineTogether.proxy.openFriendsGui();
//            }
//
//            if (button != null && button.id == CHAT_BUTTON_ID)
//            {
//                Minecraft.getMinecraft().displayGuiScreen(new GuiMTChat(gui));
//            }
//        } else if (gui instanceof ScreenChatOptions)
//        {
//            if (button == ingameChatButton)
//            {
//                boolean chatEnabled = !MineTogether.instance.ingameChat.hasDisabledIngameChat();
//                if (chatEnabled)
//                    MineTogether.proxy.disableIngameChat();
//                else
//                    MineTogether.proxy.enableIngameChat();
//
//                button.displayString = "MineTogether Chat: " + (chatEnabled ? "OFF" : "ON");
//            }
//        }
//    }
    
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event)
    {
        if (!Config.getInstance().isSivIntegration())
        {
            return;
        }
        if (event.getType() != RenderGameOverlayEvent.ElementType.PLAYER_LIST)
        {
            return;
        }
//        if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped())
//        {
//            return;
//        }
        
        Minecraft mc = Minecraft.getInstance();

//        ScaledResolution resolution = new ScaledResolution(mc);
//        guiServerInfo.setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
//        if (guiServerInfo.renderServerInfo())
//        {
//            event.setCanceled(true);
//        }
    }
    
    private boolean updateNeeded = false;
    
    private Thread updateThread = null;
    
    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
//        if (!Config.getInstance().isSivIntegration())
//            return;
////        guiServerInfo.doTick();
//        if (!((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown()) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped())
//        {
//            return;
//        }
//
//        if (updateThread == null)
//        {
//            updateThread = new Thread(() ->
//            {
//                while (true)
//                {
//                    if (updateNeeded)
//                    {
//                        updateNeeded = false;
//                        if (MineTogether.instance.getQueryGetter() != null)
//                        {
//                            MineTogether.instance.getQueryGetter().run();
//                        }
//                    }
//                    try
//                    {
//                        Thread.sleep(100);
//                    } catch (InterruptedException ignored) {}
//                }
//            });
//
//            updateThread.setDaemon(true);
//            updateThread.setName("SIP Update Thread");
//            updateThread.start();
//        }
//
//        try
//        {
//            if (ticks == 0)
//            {
//                ticks = 40;
//                updateNeeded = true;
//
//            }
//            ticks--;
//        } catch (Throwable ignored) {}
    }

//    @SubscribeEvent
//    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
//    {
//        if (!MineTogether.MOD_ID.equals(eventArgs.getModID()))
//        {
//            return;
//        }
//
//        MineTogether.instance.saveConfig(false);
//    }

//    @SubscribeEvent
//    public void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent evt)
//    {
//        MineTogether.instance.curServerId = -1;
//        CreeperHostServer.serverOn = false;
//        CreeperHostServer.updateID = -1;
//        CreeperHostServer.secret = null;
//    }
    
    @SuppressWarnings("Duplicates")
    
    public void handleToastInteraction()
    {
        Runnable method = ToastHandler.toastMethod;
        ToastHandler.clearToast(false);
        if (method != null) method.run();
    }

//    @SubscribeEvent
//    public void onKeyboardInputGui(GuiScreenEvent.KeyboardInputEvent.Pre event)
//    {
//        onKeyInputGeneric();
//    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
//        onKeyInputGeneric();
    }

//    private void onKeyInputGeneric()
//    {
//        KeyBinding binding = ((Client) MineTogether.proxy).openGuiKey;
//        boolean pressed = binding.getKeyCode() != 0 && Keyboard.isKeyDown(binding.getKeyCode()) && binding.getKeyConflictContext().isActive() && binding.getKeyModifier().isActive(binding.getKeyConflictContext());
//        if (!pressed)
//            return;
//        if (MineTogether.instance.isActiveToast())
//        {
//            handleToastInteraction();
//        } else if (Config.getInstance().isServerListEnabled())
//        {
//            MineTogether.proxy.openFriendsGui();
//        }
//    }


//    @SubscribeEvent
//    public void onMouseInputEvent(GuiScreenEvent.MouseInputEvent.Pre event) // this fires on mouse clicked in any GUI, and allows us to cancel it
//    {
//        MouseEvent mouseEvent = new MouseEvent(); // convenient shortcut to get everything we need
//        event.setCanceled(onMouseInput(mouseEvent));
//    }

//    public boolean onMouseInput(MouseEvent event)
//    {
//        Screen activeScreen = Minecraft.getInstance().currentScreen;
//        if (activeScreen == null)
//            return false; // just to stop the compiler from bitching mainly, and better to be safe than sorry
//        if (!(event.isButtonstate() && event.getButton() == 0))
//            return false;
//        if (!MineTogether.instance.isActiveToast())
//            return false;
//        int x = event.getX() * activeScreen.width / this.mc.displayWidth;
//        int y = activeScreen.height - event.getY() * activeScreen.height / this.mc.displayHeight - 1;
//        if (x > activeScreen.width - 160 && y < 32)
//        {
//            handleToastInteraction();
//            return true;
//        }
//        return false;
//    }
//
//    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
//    {
//        fakeGui.drawTexturedModalRect(x, y, textureX, textureY, width, height);
//    }
}
