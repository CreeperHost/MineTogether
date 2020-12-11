package net.creeperhost.minetogether.mtconnect;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventHandler {

    public EventHandler() {
        CompletableFuture.runAsync(() -> {
            //This needs to retry, otherwise a single failed ping for any of a great many reasons means a whole feature is disabled until a client restart...
            while(!ConnectHelper.isEnabled) {

                try {
                    boolean result = InetAddress.getByName("2a04:de41::1").isReachable(15000);
                    ConnectHelper.isEnabled = result;
//                    MineTogether.logger.info(result);
                } catch (Throwable ignored) {
                    ignored.printStackTrace();
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
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
        }
    }
}
