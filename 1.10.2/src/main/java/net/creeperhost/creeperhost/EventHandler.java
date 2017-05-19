package net.creeperhost.creeperhost;

import net.creeperhost.creeperhost.api.Order;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.gui.GuiServerInfo;
import net.creeperhost.creeperhost.gui.element.ButtonCreeper;
import net.creeperhost.creeperhost.gui.mpreplacement.CreeperHostEntry;
import net.creeperhost.creeperhost.lib.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class EventHandler {
    
    private static final int BUTTON_ID = 30051988;
    
    private static Field parentScreenField;
    private static GuiServerInfo guiServerInfo = new GuiServerInfo();
    
    
    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event) {
        
        if (!Config.getInstance().isMainMenuEnabled())
            return;
        GuiScreen gui = Util.getGuiFromEvent(event);
        if (gui instanceof GuiMainMenu) {
            CreeperHost.instance.setRandomImplementation();
            if (CreeperHost.instance.getImplementation() == null)
                return;
            List<GuiButton> buttonList = Util.getButtonList(event);
            if (buttonList != null) {
                buttonList.add(new ButtonCreeper(BUTTON_ID, gui.width / 2 + 104, gui.height / 4 + 48 + 72 + 12));
            }
        } else if (gui instanceof GuiMultiplayer) {
            if (!Config.getInstance().isMpMenuEnabled() || CreeperHost.instance.getImplementation() == null)
                return;
            // Done using reflection so we can work on 1.8.9 before setters/getters
            GuiMultiplayer mpGUI = (GuiMultiplayer) gui;
            try {
                if (serverListSelectorField == null) {
                    serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146803_h", "serverListSelector");
                    serverListSelectorField.setAccessible(true);
                }
    
                if (serverListInternetField == null) {
                    serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "field_148198_l", "serverListInternet");
                    serverListInternetField.setAccessible(true);
                }
    
                ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(mpGUI); // Get the old selector
                List serverListInternet = (List) serverListInternetField.get(serverListSelector); // Get the list from inside it
                serverListInternet.add(new CreeperHostEntry(mpGUI, new ServerData("", "127.0.0.1", false), true));
            } catch (Throwable e) {
                CreeperHost.logger.warn("Reflection to alter server list failed.", e);
            }
        }
    }
    
    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event) {
        
        if (!Config.getInstance().isMainMenuEnabled() || CreeperHost.instance.getImplementation() == null)
            return;
        GuiScreen gui = Util.getGuiFromEvent(event);
        if (gui instanceof GuiMainMenu) {
            GuiButton button = Util.getButton(event);
            if (button != null && button.id == BUTTON_ID) {
                Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }
    }
    
    private static Field serverListSelectorField;
    private static Field serverListInternetField;
    
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        
        if ((event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE && event.getType() != RenderGameOverlayEvent.ElementType.JUMPBAR) ||
                event.isCancelable()) {
            return;
        }
        if (!KeyBindings.getSivGuiKeybind().isKeyDown() || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped()) {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        guiServerInfo.renderServerInfo(event.getResolution());
    }
    
    
    private static int ticks = 0;
    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event){
        if (!KeyBindings.getSivGuiKeybind().isKeyDown() || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped()) {
            return;
        }
        if(ticks == 0){
            ticks = 40;
            //Update
            if(CreeperHost.instance.getQueryGetter() != null) {
                CreeperHost.instance.getQueryGetter().run();
            }
        }
        ticks--;
    }
    
}
