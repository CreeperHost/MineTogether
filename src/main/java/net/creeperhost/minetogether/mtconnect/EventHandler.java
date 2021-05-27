package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public void guiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiIngameMenu) {
            IntegratedServer integratedServer = Minecraft.getMinecraft().getIntegratedServer();
            if (integratedServer != null) {
                Object value = ObfuscationReflectionHelper.getPrivateValue(IntegratedServer.class, integratedServer, "isPublic", "field_71346_p");
                GuiButton guiButton = new GuiButton(-69420, gui.width / 2 - 100, gui.height / 4 + 72 + -16, 98, 20, I18n.format("minetogether.connect.open"));
                guiButton.enabled = ConnectHelper.isEnabled && !(boolean) value;
                event.getButtonList().add(guiButton);
                for (GuiButton b : event.getButtonList()) {
                    if (b.id == 7)//Open to LAN
                    {
                        b.width = 98;
                        b.xPosition += 102;
                        break;
                    }
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
