/*package net.creeperhost.minetogether.module.connect;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import java.util.concurrent.CompletableFuture;

public class EventHandler {

    public EventHandler() {
        CompletableFuture.runAsync(net.creeperhost.minetogether.mtconnect.ConnectHandler::connectToProc);
    }


    @SubscribeEvent
    public void guiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen gui = event.getGui();
        if (gui instanceof IngameMenuScreen) {
            IntegratedServer integratedServer = Minecraft.getInstance().getIntegratedServer();
            if (integratedServer != null) {
                Widget feedBack = ScreenUtils.removeButton("menu.sendFeedback", event.getWidgetList());
                Widget bugs = ScreenUtils.removeButton("menu.reportBugs", event.getWidgetList());
                Widget openToLan = ScreenUtils.findButton("menu.shareToLan", event.getWidgetList());
                Widget options = ScreenUtils.findButton("menu.options", event.getWidgetList());

                if(openToLan != null)
                {
                    openToLan.y = feedBack.y;
                }

                Button guiButton = new Button(gui.width / 2 - 100, gui.height / 4 + 72 + -16, 98, 20, new StringTextComponent(I18n.format("minetogether.connect.open")), (button) ->
                {
                    Minecraft.getInstance().displayGuiScreen(new GuiShareToFriends(gui));
                });
                Button ourFeedback = new Button(bugs.x, options.y, feedBack.getWidth(), 20, new StringTextComponent(I18n.format("menu.reportBugs")), (button) ->
                {
                    String s = Config.getInstance().getIssueTrackerUrl();
                    Minecraft.getInstance().displayGuiScreen(new ConfirmOpenLinkScreen((p_213069_2_) -> {
                        if (p_213069_2_) {
                            Util.getOSType().openURI(s);
                        }

                        Minecraft.getInstance().displayGuiScreen(event.getGui());
                    }, s, true));
                });
                guiButton.active = ConnectHelper.isEnabled && !integratedServer.getPublic();
                event.addWidget(guiButton);
                event.addWidget(ourFeedback);
            }
        } else if (gui instanceof MultiplayerScreen) {
            if(ConnectHelper.isEnabled) {
                MultiplayerScreen guiMp = (MultiplayerScreen) gui;
                if (!(guiMp.lanServerList instanceof FriendsServerList)) {
                    guiMp.lanServerList = new FriendsServerList(guiMp.lanServerList, guiMp);
                }
            }
        }
    }

    @SubscribeEvent()
    public void serverShutdown(FMLServerStoppingEvent ev) {
        MinecraftServer server = ev.getServer();
        if(server instanceof IntegratedServer) {
            if(ConnectHelper.isShared((IntegratedServer)server)) {
                net.creeperhost.minetogether.mtconnect.ConnectHandler.close();
            }
        }
    }
}
*/