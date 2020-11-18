package net.creeperhost.minetogether.mtconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {
    @SubscribeEvent
    public void guiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiIngameMenu) {
            GuiButton guiButton = new GuiButton(-69420, 0, 0, 70, 20, I18n.format("minetogether.connect"));
            event.getButtonList().add(guiButton);
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
