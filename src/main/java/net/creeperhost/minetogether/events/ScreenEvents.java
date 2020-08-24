package net.creeperhost.minetogether.events;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.MinigamesScreen;
import net.creeperhost.minetogether.client.screen.SettingsScreen;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.client.screen.chat.ingame.GuiNewChatOurs;
import net.creeperhost.minetogether.client.screen.element.GuiButtonCreeper;
import net.creeperhost.minetogether.client.screen.element.GuiButtonMultiple;
import net.creeperhost.minetogether.client.screen.order.GuiGetServer;
import net.creeperhost.minetogether.client.screen.serverlist.gui.MultiplayerPublicScreen;
import net.creeperhost.minetogether.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class ScreenEvents
{
    private static final Logger logger = LogManager.getLogger();
    boolean first = true;

    private boolean firstConnect = true;

    @SubscribeEvent
    public void openScreen(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if(!MineTogether.isOnline) return;
        boolean buttonDrawn = false;
        
        if (event.getGui() instanceof MainMenuScreen)
        {
            if (MineTogether.instance.gdpr.hasAcceptedGDPR() && first)
            {
                first = false;
                MineTogether.proxy.startChat();
                String server = System.getProperty("mt.server");
                String port = System.getProperty("mt.port", "25565");
                int realPort = -1;
                if (server != null)
                {
                    try {
                        realPort = Integer.parseInt(port);
                    } catch (Throwable t) {
                        logger.error("Unable to auto connect to server as unable to parse port " + port, t);
                    }

                    if (realPort != -1) Minecraft.getInstance().displayGuiScreen(new ConnectingScreen(event.getGui(), Minecraft.getInstance(), server, realPort));
                }
            }
            if (Config.getInstance().isServerListEnabled() || Config.getInstance().isChatEnabled())
            {
                MineTogether.instance.setRandomImplementation();
                
                event.addWidget(new GuiButtonCreeper(event.getGui().width / 2 - 124, event.getGui().height / 4 + 96, p ->
                {
                    Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
                }));
            }
            //Replace Multiplayer Button
            AtomicInteger width = new AtomicInteger();
            AtomicInteger height = new AtomicInteger();
            AtomicInteger x = new AtomicInteger();
            AtomicInteger y = new AtomicInteger();
            
            event.getWidgetList().forEach(widget ->
            {
                if (widget instanceof Button)
                {
                    Button button = (Button) widget;
                    //Get the translated name so we can make sure to remove the correct button
                    String name = I18n.format("menu.multiplayer");
                    
                    if (button.getMessage().equalsIgnoreCase(name))
                    {
                        width.set(button.getWidth());
                        height.set(button.getHeight());
                        x.set(button.x);
                        y.set(button.y);
                        
                        button.active = false;
                        button.visible = false;
                    }
                }
            });
            
            event.addWidget(new Button(x.get(), y.get(), width.get(), height.get(), I18n.format("menu.multiplayer"), p ->
            {
                if (MineTogether.instance.gdpr.hasAcceptedGDPR())
                {
                    Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(event.getGui()));
                } else
                {
                    Minecraft.getInstance().displayGuiScreen(new MultiplayerScreen(event.getGui()));
                }
            }));
        }
        
        if (event.getGui() instanceof MultiplayerScreen || event.getGui() instanceof MultiplayerPublicScreen)
        {
            event.getWidgetList().forEach(b ->
            {
                if (b instanceof Button)
                {
                    if (b.getMessage().equalsIgnoreCase(I18n.format("selectServer.refresh")))
                    {
                        b.active = false;
                        b.visible = false;
                    }
                    if (b.getMessage().equalsIgnoreCase(I18n.format("selectServer.delete")))
                    {
                        b.x -= 7;
                        b.setWidth(b.getWidth() + 1);
                    }
                    if (b.getMessage().equalsIgnoreCase(I18n.format("selectServer.direct")))
                    {
                        b.x = event.getGui().width / 2 - 8;
                        b.y = event.getGui().height - 28;
                        b.setWidth(b.getWidth() - 14);
                    }
                    if (b.getMessage().equalsIgnoreCase(I18n.format("selectServer.add")))
                    {
                        b.x -= 25;
                    }
                    if (b.getMessage().equalsIgnoreCase(I18n.format("selectServer.cancel")))
                    {
                        b.x += 1;
                        b.setWidth(b.getWidth() - 2);
                    }
                }
            });
            
            event.addWidget(new GuiButtonMultiple(event.getGui().width / 2 + 133, event.getGui().height - 52, 2, p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(((MultiplayerPublicScreen) event.getGui()).parent, ((MultiplayerPublicScreen) event.getGui()).listType, ((MultiplayerPublicScreen) event.getGui()).sortOrder));
            }));
            
            event.addWidget(new Button(event.getGui().width / 2 - 50, event.getGui().height - 52, 75, 20, "Minigames", p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MinigamesScreen(event.getGui()));
            }));
        }
        
        if (event.getGui() instanceof MainMenuScreen)
        {
            if (Config.getInstance().isEnableMainMenuFriends())
            {
                buttonDrawn = true;
                event.addWidget(new Button(event.getGui().width - 100 - 5, 5, 100, 20, I18n.format("creeperhost.multiplayer.friends"), p ->
                {
                    MineTogether.proxy.openFriendsGui();
                }));
                
                int x = event.getGui().width - 20 - 5;
                if (buttonDrawn) x -= 99;
                
                event.addWidget(new GuiButtonMultiple(x, 5, 1, p ->
                {
                    if (Config.getInstance().isChatEnabled()) {
                        Minecraft.getInstance().displayGuiScreen(new MTChatScreen(event.getGui()));
                    } else {
                        Minecraft.getInstance().displayGuiScreen(new SettingsScreen(event.getGui()));
                    }
                }));
            }
            
            //TEST
//            event.addWidget(new Button(event.getGui().width / 2 - 50, 5, 100, 20, I18n.format("Universe 7"), p ->
//            {
//                WorldHandler worldHandler = new WorldHandler();
//                worldHandler.createWorld();
//            }));
        }
        
        if (event.getGui() instanceof IngameMenuScreen)
        {
            buttonDrawn = true;
            event.addWidget(new Button(event.getGui().width - 100 - 5, 5, 100, 20, I18n.format("creeperhost.multiplayer.friends"), p ->
            {
                MineTogether.proxy.openFriendsGui();
            }));
            
            int x = event.getGui().width - 20 - 5;
            if (buttonDrawn) x -= 99;
            
            event.addWidget(new GuiButtonMultiple(x, 5, 1, p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MTChatScreen(event.getGui()));
            }));
        }
    }
    
    boolean firstOpen = true;
    
    @SubscribeEvent
    public void openScreen(GuiOpenEvent event)
    {
        if(!MineTogether.isOnline) return;
        Screen screen = event.getGui();
        
        if (screen instanceof ChatScreen && Config.getInstance().isChatEnabled() && !MineTogether.instance.ingameChat.hasDisabledIngameChat() && MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            String presetString = "";
            boolean sleep = false;
            if (screen instanceof SleepInMultiplayerScreen)
            {
                sleep = true;
            }
            
            ChatScreen chatScreen = (ChatScreen) event.getGui();
            presetString = chatScreen.defaultInputFieldText;
            MinecraftServer minecraftServerInstance = MineTogether.server;
            
            if (Config.getInstance().isAutoMT() && minecraftServerInstance != null && minecraftServerInstance.isSinglePlayer() && Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs && firstOpen)
            {
                firstOpen = false;
                ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setBase(!MineTogether.instance.gdpr.hasAcceptedGDPR());
            }
            try
            {
                event.setGui(new GuiChatOurs(presetString, sleep));
            } catch (Exception ignored)
            {
            }
        }
    }
}
