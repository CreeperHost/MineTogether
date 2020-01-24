package net.creeperhost.minetogether.events;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.gui.GuiGDPR;
import net.creeperhost.minetogether.client.gui.GuiMinigames;
import net.creeperhost.minetogether.client.gui.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.client.gui.chat.ingame.GuiNewChatOurs;
import net.creeperhost.minetogether.client.gui.element.GuiButtonCreeper;
import net.creeperhost.minetogether.client.gui.element.GuiButtonMultiple;
import net.creeperhost.minetogether.client.gui.order.GuiGetServer;
import net.creeperhost.minetogether.client.gui.serverlist.gui.GuiMultiplayerPublic;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.ServerLifecycleEvent;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScreenEvents
{
    boolean first = true;
    
    @SubscribeEvent
    public void openScreen(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (event.getGui() instanceof MainMenuScreen)
        {
            if (!MineTogether.instance.gdpr.hasAcceptedGDPR())
            {
                Minecraft.getInstance().currentScreen = new GuiGDPR(event.getGui());
            } else
            {
                if (first)
                {
                    first = false;
                    MineTogether.proxy.startChat();
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
                Minecraft.getInstance().displayGuiScreen(new GuiMultiplayerPublic(event.getGui()));
            }));
        }

        if(event.getGui() instanceof MultiplayerScreen || event.getGui() instanceof GuiMultiplayerPublic)
        {
            event.getWidgetList().forEach(b ->
            {
                if(b instanceof Button)
                {
                    if(b.getMessage().equalsIgnoreCase(I18n.format("selectServer.refresh")))
                    {
                        b.active = false;
                        b.visible = false;
                    }
                    if(b.getMessage().equalsIgnoreCase(I18n.format("selectServer.delete")))
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
                Minecraft.getInstance().displayGuiScreen(new GuiMultiplayerPublic(new MainMenuScreen()));
            }));

            event.addWidget(new Button(event.getGui().width / 2 - 50, event.getGui().height - 52, 75, 20, "Minigames", p ->
            {
                Minecraft.getInstance().displayGuiScreen(new GuiMinigames(event.getGui()));
            }));
            MultiplayerScreen multiplayerScreen = (MultiplayerScreen) event.getGui();
            ServerSelectionList serverSelectionList = multiplayerScreen.serverListSelector;
            ServerSelectionList.LanDetectedEntry entry = new ServerSelectionList.LanDetectedEntry();

            multiplayerScreen.serverListSelector.serverListLan.add();
        }
    }

    boolean firstOpen = true;

    @SubscribeEvent
    public void openScreen(GuiOpenEvent event)
    {
        Screen screen = event.getGui();

        if (screen instanceof ChatScreen && Config.getInstance().isChatEnabled() && !MineTogether.instance.ingameChat.hasDisabledIngameChat())
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
            } catch (Exception ignored){}
        }
    }
}
