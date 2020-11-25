package net.creeperhost.minetogether.mtconnect;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public class EventHandler {
    public EventHandler() {
        CompletableFuture.runAsync(() -> {
            //This needs to retry, otherwise a single failed ping for any of a great many reasons means a whole feature is disabled until a client restart...
            while(!ConnectHelper.isEnabled) {

                try {
                    boolean result = InetAddress.getByName("2a04:de41::1").isReachable(15000);
                    ConnectHelper.isEnabled = result;
                    CreeperHost.logger.info(result);
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
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiIngameMenu) {
            GuiButton guiButton = new GuiButton(-69420, gui.width / 2 - 100, gui.height / 4 + 72 + -16, 98, 20, I18n.format("minetogether.connect.open"));
            guiButton.enabled = ConnectHelper.isEnabled;
            event.getButtonList().add(guiButton);
            for(GuiButton b : event.getButtonList())
            {
                if(b.id == 7)//Open to LAN
                {
                    b.width = 98;
                    b.xPosition += 102;
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void actionPerformed(GuiScreenEvent.ActionPerformedEvent event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiIngameMenu && event.getButton().id == -69420) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiShareToFriends(gui));
        }
    }
}
